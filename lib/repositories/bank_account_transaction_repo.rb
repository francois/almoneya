require "repositories/bank_account_transaction"
require "set"
require "yaml"

module Repositories
  class BankAccountTransactionRepo
    def initialize(bank_accounts_dataset:, bank_account_transactions_dataset:)
      @bank_accounts_ds             = bank_accounts_dataset
      @bank_account_transactions_ds = bank_account_transactions_dataset
    end

    attr_reader :bank_accounts_ds, :bank_account_transactions_ds
    private :bank_accounts_ds, :bank_account_transactions_ds

    IMPORT_ATTRS      = %i[          account_num       check_num    posted_on description1 description2 amount]
    IMPORT_COLUMNS    = %i[tenant_id bank_account_hash check_number posted_on description1 description2 amount]
    RETURNING_COLUMNS = %i[bank_account_transaction_id bank_account_hash check_number posted_on description1 description2 amount created_at updated_at]

    def import(tenant_id:, bank_accounts:, transactions:)
      existing_hashes      = bank_accounts_ds.distinct.select_map(:bank_account_hash).to_set
      missing_hashes       = transactions.reject{|txn| existing_hashes.include?(txn.account_num)}.map(&:account_num).uniq
      missing_accounts     = missing_hashes.map{|missing_acct_num| bank_accounts.detect{|bank_account| bank_account.account_num == missing_acct_num}}
      raise "ASSERTION ERROR: Failed to find all missing_hashes in missing_accounts: #{missing_hashes.inspect}; missing_accounts; #{missing_accounts}" unless missing_hashes.compact.size == missing_accounts.compact.size
      missing_account_rows = missing_accounts.map{|account| [tenant_id, account.account_num, account.last4]}
      bank_accounts_ds.import([:tenant_id, :bank_account_hash, :bank_account_last4], missing_account_rows) if missing_accounts.any?

      results = bank_account_transactions_ds.
        returning(*RETURNING_COLUMNS).
        import(IMPORT_COLUMNS, transactions.map{|txn| [tenant_id] + IMPORT_ATTRS.map{|attr| txn.public_send(attr)}})
      results.map do |result|
        BankAccountTransaction.new(*RETURNING_COLUMNS.map{|col| result.fetch(col)})
      end
    end
  end
end
