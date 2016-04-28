require "repositories/not_found"
require "repositories/userpass_credentials"

module Repositories
  class UserRepo
    def initialize(userpass_dataset:)
      @userpass_ds = userpass_dataset
    end

    attr_reader :userpass_ds
    private :userpass_ds

    def find_userpass_credentials_by_username(username)
      row = userpass_ds[username: username]
      if row then
        UserpassCredentials.new(
          row.fetch(:user_id),
          row.fetch(:username),
          row.fetch(:password_hash))
      else
        raise NotFound
      end
    end
  end
end
