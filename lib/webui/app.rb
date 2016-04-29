# Foundational requires
require "json"
require "logger"
require "rack"
require "sequel"
require "sinatra/base"
require "tilt/erb"

# This application's possible actions
require "operations/sign_in"
require "repositories/account_repo"
require "repositories/sign_in_repo"
require "repositories/user_repo"
require "schemas/new_account_schema"
require "schemas/userpass_sign_in_schema"

module Webui
  class App < Sinatra::Base
    disable :sessions # We use Rack::Cookie::Session directly in config.ru
    disable :logging

    configure :development do
      enable :static
      set :public_folder, File.expand_path("../../../public", __FILE__)
    end

    configure do
      DB = Sequel.connect(ENV["DATABASE_URL"] || "postgresql:///vagrant", logger: Logger.new(STDERR))

      accounts_dataset = DB[:public__accounts]
      sign_ins_dataset = DB[:credentials__sign_ins]
      userpass_dataset = DB[:credentials__user_userpass_credentials]
      users_dataset    = DB[:public__users]
      userpass_sign_ins_dataset = DB[:credentials__userpass_sign_ins]

      account_repo = Repositories::AccountRepo.new(accounts_dataset: accounts_dataset)
      sign_in_repo = Repositories::SignInRepo.new(sign_ins_dataset: sign_ins_dataset, userpass_sign_ins_dataset: userpass_sign_ins_dataset)
      user_repo    = Repositories::UserRepo.new(userpass_dataset: userpass_dataset, users_dataset: users_dataset)

      set :operations, {
        sign_in_op: Operations::SignIn.new(sign_in_repo: sign_in_repo, user_repo: user_repo),
      }

      set :repositories, {
        account_repo: account_repo,
        sign_in_repo: sign_in_repo,
        user_repo: user_repo,
      }
    end

    before do
      redirect to("/sign-in") unless signed_in? || %w(/sign-in /contact/support).include?(request.path_info)
    end

    before { @search = true }

    get "/" do
      @accounts = account_repo.find_all_for_tenant(authenticated_user.tenant_id)
      erb :dashboard, layout: :application
    end

    get "/sign-in" do
      session.clear
      @failed = (params[:failed] == "1")
      @search = false
      erb :sign_in, layout: :application
    end

    post "/sign-in" do
      session.clear
      begin
        result = Schemas::UserpassSignInSchema.call(
          username: params[:sign_in][:username],
          password: params[:sign_in][:password])
        if result.success? then
          valid_sign_in = result.output
          sign_in = DB.transaction do
            sign_in_op.sign_in_with_username_and_password(
              username: valid_sign_in.fetch(:username),
              password: valid_sign_in.fetch(:password),
              source_ip: request.ip,
              user_agent: request.user_agent)
          end
          session[:authenticated_user_id] = sign_in.user_id
          redirect to("/")
        else
          redirect to("/sign-in?failed=1")
        end
      rescue Operations::SignIn::UnknownUsername
        logger.warn "Authentication failure: unknown username"
        redirect to("/sign-in?failed=1")
      rescue Operations::SignIn::InvalidCredentials
        logger.warn "Authentication failure: invalid credentials"
        redirect to("/sign-in?failed=1")
      end
    end

    post "/api/accounts" do
      result = Schemas::NewAccountSchema.call(
        code: params[:account][:code],
        name: params[:account][:name],
        kind: params[:account][:kind])
      if result.success? then
        valid_account = result.output
        account = Repositories::Account.new(
          nil,
          valid_account.fetch(:code),
          valid_account.fetch(:name),
          valid_account.fetch(:kind))
        account = DB.transaction { account_repo.create(tenant_id: authenticated_user.tenant_id, account: account) }
        output = {
          "account" => {
            "account_id" => account.account_id,
            "code"       => account.code,
            "name"       => account.name,
            "kind"       => account.kind,
            "created_at" => account.created_at,
            "updated_at" => account.updated_at}}
        json output, code: :created, location: url("/api/accounts/#{account.account_id}")
      else
        json result.messages, code: :bad_request
      end
    end

    helpers do
      CODES = {
        200          => 200,
        201          => 201,
        400          => 400,
        :bad_request => 400,
        :created     => 201,
        :ok          => 200,
      }

      def json(object, code: :created, location: nil)
        headers = {"Content-Type" => "application/json; charset=utf-8"}
        headers["Location"] = location if location
        [CODES.fetch(code), headers, JSON.dump(object)]
      end

      def signed_in?
        !!session[:authenticated_user_id]
      end

      def authenticated_user
        @authenticated_user ||= user_repo.find_by_id(session[:authenticated_user_id])
      end

      def logger
        DB.loggers.first
      end

      def locale
        I18n.locale
      end

      def h(str)
        Rack::Utils.escape_html(str)
      end
    end

    def user_repo
      settings.repositories.fetch(:user_repo)
    end

    def account_repo
      settings.repositories.fetch(:account_repo)
    end

    def sign_in_op
      settings.operations.fetch(:sign_in_op)
    end
  end
end
