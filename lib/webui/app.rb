# Foundational requires
require "logger"
require "sequel"
require "sinatra/base"
require "tilt/erb"

# This application's operations
require "operations/sign_in"
require "repositories/sign_in_repo"
require "repositories/user_repo"

module Webui
  class App < Sinatra::Base
    enable :sessions
    disable :logging

    configure do
      DB = Sequel.connect(ENV["DATABASE_URL"] || "postgresql:///vagrant", logger: Logger.new(STDERR))

      sign_ins_dataset = DB[:credentials__sign_ins]
      userpass_dataset = DB[:credentials__user_userpass_credentials]
      users_dataset    = DB[:public__users]
      userpass_sign_ins_dataset = DB[:credentials__userpass_sign_ins]

      sign_in_repo = Repositories::SignInRepo.new(sign_ins_dataset: sign_ins_dataset, userpass_sign_ins_dataset: userpass_sign_ins_dataset)
      user_repo    = Repositories::UserRepo.new(userpass_dataset: userpass_dataset, users_dataset: users_dataset)

      set :operations, {
        sign_in_op: Operations::SignIn.new(sign_in_repo: sign_in_repo, user_repo: user_repo),
      }

      set :repositories, {
        user_repo: user_repo,
      }
    end

    get "/" do
      redirect to("/sign-in")
    end

    get "/sign-in" do
      @failed = (params[:failed] == "1")
      erb :sign_in
    end

    post "/sign-in" do
      logger.info sign_in_op.inspect

      begin
        sign_in = sign_in_op.sign_in_with_username_and_password(
          username: params[:sign_in][:username],
          password: params[:sign_in][:password],
          source_ip: request.ip,
          user_agent: request.user_agent)
        session[:authenticated_user_id] = sign_in.user_id
        redirect to("/dashboard")
      rescue Operations::SignIn::UnknownUsername
        logger.warn "Authentication failure: unknown username"
        redirect to("/sign-in?failed=1")
      rescue Operations::SignIn::InvalidCredentials
        logger.warn "Authentication failure: invalid credentials"
        redirect to("/sign-in?failed=1")
      end
    end

    get "/dashboard" do
      erb :dashboard
    end

    helpers do
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
    end

    def user_repo
      settings.repositories.fetch(:user_repo)
    end

    def sign_in_op
      settings.operations.fetch(:sign_in_op)
    end
  end
end
