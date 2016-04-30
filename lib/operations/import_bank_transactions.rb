require "csv"
require "digest/sha2"
require "repositories/bank_account"
require "repositories/bank_account_transaction"

module Operations
  class ImportBankTransactions
    FileTooLarge  = Class.new(StandardError)
    InvalidFormat = Class.new(StandardError)
    UnknownFormat = Class.new(StandardError)

    def initialize(bank_account_transaction_repo:)
      @bank_account_transaction_repo = bank_account_transaction_repo
    end

    attr_reader :bank_account_transaction_repo
    private :bank_account_transaction_repo

    THRESHOLD = 5_000_000

    def call(tenant_id:, file:)
      raise FileTooLarge,  "File #{File.basename(file.path)} exceeds the threshold of #{"%.1f" % [THRESHOLD / 1_000_000]} MB; won't process" if file.size > THRESHOLD
      raise InvalidFormat, "File #{File.basename(file.path)} is empty" if file.size.zero?

      # TODO: We have to determine which bank generated this 
      rows = CSV.read(file.path, encoding: "ISO-8859-1:UTF-8", row_sep: "\r\n", col_sep: ",", headers: false)
      bank_accounts, transactions =
        case rows.last.size
        when  8 ; parse_rbc(rows)
        when 14 ; parse_desjardins(rows)
        else
          raise UnknownFormat, "Cannot parse #{File.basename(file.path)}: we do not recognize files with #{rows.first.size} columns in them"
        end

      bank_account_transaction_repo.import(
        tenant_id:     tenant_id,
        bank_accounts: bank_accounts.compact.uniq,
        transactions:  transactions.compact)
    rescue CSV::MalformedCSVError => e
      raise InvalidFormat, "File #{File.basename(file.path)} is not a CSV file: #{e.message}"
    end

    def parse_rbc(rows)
      bank_accounts = rows.map do |row|
        next if row[0] == "Type de compte"

        account_num = Digest::SHA256.hexdigest("#{row[0]} #{row[1]}")
        last4       = row[1][-4..-1]
        Repositories::BankAccount.new(nil, account_num, last4)
      end

      transactions = rows.map do |row|
        next if row[0] == "Type de compte"

        account_num = Digest::SHA256.hexdigest("#{row[0]} #{row[1]}")
        Repositories::BankAccountTransaction.new(
          nil,
          account_num,
          row[3],             # check_number
          Date.parse(row[2]),             # posted_on
          row[4],             # description1
          row[5],             # description2
          BigDecimal(row[6])) # amount
      end

      [bank_accounts, transactions]
    end
    private :parse_rbc

    def parse_desjardins(rows)
      bank_accounts = rows.map do |row|
        next if row.map(&:strip).all?(&:empty?)

        account_num = Digest::SHA256.hexdigest(row[0, 3].join(" "))
        last4       = row[1][-4..-1]
        Repositories::BankAccount.new(nil, account_num, last4)
      end

      transactions = rows.map do |row|
        next if row.map(&:strip).all?(&:empty?)

        account_num = Digest::SHA256.hexdigest(row[0, 3].join(" "))
        Repositories::BankAccountTransaction.new(
          nil,
          account_num,
          row[6],             # check_number
          Date.parse(row[3]), # posted_on
          row[5],             # description1
          nil,                # description2
          row[7].strip.empty? ? BigDecimal(row[8]) : -1 * BigDecimal(row[7]) # amount
        )
      end

      [bank_accounts, transactions]
    end
    private :parse_desjardins
  end
end
