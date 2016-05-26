module Repositories
  BankAccountTransaction = Struct.new(:bank_account_transaction_id, :account_num, :check_num, :posted_on, :description1, :description2, :amount, :created_at, :updated_at) do
    alias_method :id, :bank_account_transaction_id
  end
end
