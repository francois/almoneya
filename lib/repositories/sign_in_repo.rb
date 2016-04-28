require "repositories/sign_in"

module Repositories
  class SignInRepo
    def initialize(sign_ins_dataset:, userpass_sign_ins_dataset:)
      @sign_ins_ds = sign_ins_dataset
      @userpass_sign_ins_ds = userpass_sign_ins_dataset
    end

    attr_reader :sign_ins_ds, :userpass_sign_ins_ds
    private :sign_ins_ds, :userpass_sign_ins_ds

    # Records an attempt at using a username and password to authenticate.
    #
    # Sign ins track what IP address attempted to sign in to the platform. Remembering
    # who attempted to sign in allows us to implement rate limiting.
    #
    # @param username The username that was used to attempt authentication.
    # @param source_ip The IP address of the connection that tried to sign in.
    # @param user_agent The value of the HTTP User-Agent header in the request.
    # @param successful Whether the authentication attempt was successful or not.
    #
    # @return A SignIn instance.
    def create_username_password_authentication_attempt(username:, source_ip:, user_agent:, successful:)
      # TODO: find a way to build a transaction around these two calls to the database
      sign_in = sign_ins_ds.
        returning(:sign_in_id, :source_ip, :user_agent, :method, :successful, :created_at).
        insert(source_ip: source_ip, user_agent: user_agent, method: "userpass", successful: successful).
        first

      userpass_sign_ins_ds.insert(sign_in_id: sign_in.fetch(:sign_in_id), username: username)

      SignIn.new(sign_in.fetch(:sign_in_id),
                 sign_in.fetch(:source_ip),
                 sign_in.fetch(:user_agent),
                 sign_in.fetch(:method),
                 sign_in.fetch(:successful),
                 sign_in.fetch(:created_at))
    end
  end
end
