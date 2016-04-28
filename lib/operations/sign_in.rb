require "bcrypt"

module Operations
  class SignIn
    def initialize(sign_in_repo:, user_repo:)
      @sign_in_repo = sign_in_repo
      @user_repo = user_repo
    end

    attr_reader :sign_in_repo, :user_repo
    private :sign_in_repo, :user_repo

    # Attempts to authenticate the credentials against the database.
    #
    # @param username The principal with which the authentication attempt is being made
    # @param password The plain-text password with which the authentication attempt is being made
    # @param source_ip The IP address from which the sign in attempt allegedly is coming from
    # @param user_agent The self-identified User-Agent HTTP header in the request
    # @param method The method by which the sign in attempt was made, one of "userpass", "facebook" or "twitter"
    #
    # @return A Model::SignIn instance, describing the sign in attempt
    #
    # @raise UnknownUsername
    # @raise InvalidCredentials
    def sign_in_with_username_and_password(username:, password:, source_ip:, user_agent:)
      successful = false
      user =
        begin
          user_repo.find_userpass_credentials_by_username(username)
        rescue Repositories::NotFound => e
          raise UnknownUsername, e.message
        end

      successful = BCrypt::Password.new(user.password_hash) == password
      raise InvalidCredentials unless successful

    ensure
      sign_in = sign_in_repo.create_username_password_authentication_attempt(
        source_ip: source_ip,
        user_agent: user_agent,
        username: username,
        successful: successful)
      if sign_in.successful? then
        return sign_in
      else
        raise
      end
    end

    UnknownUsername    = Class.new(StandardError)
    InvalidCredentials = Class.new(StandardError)
  end
end
