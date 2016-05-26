module Repositories
  SignIn = Struct.new(:sign_in_id, :source_ip, :user_agent, :method, :successful, :created_at, :user_id) do
    alias_method :id, :sign_in_id
    alias_method :successful?, :successful
  end
end
