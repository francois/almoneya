module Repositories
  UserpassCredentials = Struct.new(:user_id, :username, :password_hash) do
    alias_method :id, :user_id
  end
end
