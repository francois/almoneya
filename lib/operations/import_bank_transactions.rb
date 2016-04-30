require "csv"
require "repositories/bank_account"
require "repositories/bank_account_transaction"

module Operations
  class ImportBankTransactions
    FileTooLarge  = Class.new(StandardError)
    InvalidFormat = Class.new(StandardError)
    UnknownFormat = Class.new(StandardError)

    def initialize(bank_account_transaction_repo:, desjardins_parser:, rbc_parser:)
      @bank_account_transaction_repo = bank_account_transaction_repo
      @desjardins_parser = desjardins_parser
      @rbc_parser = rbc_parser
    end

    attr_reader :bank_account_transaction_repo, :desjardins_parser, :rbc_parser
    private :bank_account_transaction_repo, :desjardins_parser, :rbc_parser

    THRESHOLD = 5_000_000

    def call(tenant_id:, file:)
      raise FileTooLarge,  "File #{File.basename(file.path)} exceeds the threshold of #{"%.1f" % [THRESHOLD / 1_000_000]} MB; won't process" if file.size > THRESHOLD
      raise InvalidFormat, "File #{File.basename(file.path)} is empty" if file.size.zero?

      # TODO: We have to determine which bank generated this 
      rows = CSV.read(file.path, encoding: "ISO-8859-1:UTF-8", row_sep: "\r\n", col_sep: ",", headers: false)
      bank_accounts, transactions =
        case rows.last.size
        when 8..9 ; rbc_parser.call(rows)
        when 14   ; desjardins_parser.call(rows)
        else
          raise UnknownFormat, "Cannot parse #{File.basename(file.path)}: we do not recognize files with #{rows.last.size} columns in them"
        end

      bank_account_transaction_repo.import(
        tenant_id:     tenant_id,
        bank_accounts: bank_accounts.compact.uniq,
        transactions:  transactions.compact)
    rescue CSV::MalformedCSVError => e
      raise InvalidFormat, "File #{File.basename(file.path)} is not a CSV file: #{e.message}"
    end
  end
end
