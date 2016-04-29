require "csv"
require "digest/sha2"
require "repositories/bank_account"
require "repositories/bank_account_transaction"

module Operations
  class ImportBankTransactions
    FileTooLarge  = Class.new(StandardError)
    InvalidFormat = Class.new(StandardError)

    def initialize(bank_account_transaction_repo:)
      @bank_account_transaction_repo = bank_account_transaction_repo
    end

    attr_reader :bank_account_transaction_repo
    private :bank_account_transaction_repo

    THRESHOLD = 5_000_000

    def call(tenant_id:, file:)
      raise FileTooLarge, "File #{File.basename(file.path)} exceeds the threshold of #{"%.1f" % [THRESHOLD / 1_000_000]} MB; won't process" if file.size > THRESHOLD
      raise InvalidFormat, "File #{File.basename(file.path)} is empty" if file.size.zero?

      # TODO: We have to determine which bank generated this 
      rows = CSV.read(file.path, encoding: "ISO-8859-1:UTF-8", row_sep: "\r\n", col_sep: ",", headers: false)
      bank_accounts = rows.map do |row|
        next if row[0] == "Type de compte"

        account_num = Digest::SHA256.hexdigest("#{row[0]} #{row[1]}")
        last4       = row[1][-4..-1]
        Repositories::BankAccount.new(nil, account_num, last4)
      end.compact

      transactions = rows.map do |row|
        next if row[0] == "Type de compte"

        account_num = Digest::SHA256.hexdigest("#{row[0]} #{row[1]}")
        Repositories::BankAccountTransaction.new(
          nil,
          account_num,
          row[3],             # check_number
          row[2],             # posted_on
          row[4],             # description1
          row[5],             # description2
          BigDecimal(row[6])) # amount
      end.compact

      bank_account_transaction_repo.import(
        tenant_id:     tenant_id,
        bank_accounts: bank_accounts,
        transactions:  transactions)
    rescue CSV::MalformedCSVError => e
      raise InvalidFormat, "File #{File.basename(file.path)} is not a CSV file: #{e.message}"
    end
  end
end
