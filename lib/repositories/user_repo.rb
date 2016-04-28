require "repositories/not_found"
require "repositories/user"
require "repositories/userpass_credentials"

module Repositories
  class UserRepo
    def initialize(userpass_dataset:, users_dataset:)
      @userpass_ds = userpass_dataset
      @users_ds = users_dataset
    end

    attr_reader :userpass_ds, :users_ds
    private :userpass_ds, :users_ds

    # Returns the User instance that corresponds to the ID, or raises a NotFound exception.
    # 
    # @param user_id The id that corresponds to a user.
    # @raise NotFound When the id does not match any existing user.
    def find_by_id(user_id)
      row = users_ds[user_id: user_id]
      if row then
        User.new(row.fetch(:user_id), row.fetch(:surname), row.fetch(:rest_of_name))
      else
        raise NotFound, "No user with ID #{user_id.inspect} found!"
      end
    end

    # Returns the credentials that correspond to the username, or raises a NotFound exception.
    #
    # @param username The username to search credentials for.
    # @raise NotFound When the username does not match any existing user.
    def find_userpass_credentials_by_username(username)
      row = userpass_ds[username: username]
      if row then
        UserpassCredentials.new(
          row.fetch(:user_id),
          row.fetch(:username),
          row.fetch(:password_hash))
      else
        raise NotFound, "No user with a username of #{username.inspect} found!"
      end
    end
  end
end
