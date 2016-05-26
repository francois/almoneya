module Repositories
  BankAccount = Struct.new(:bank_account_id, :account_num, :last4, :account, :created_at, :updated_at) do
    alias_method :id, :bank_account_id
  end
end
