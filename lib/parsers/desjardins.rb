require "digest/sha2"
require "repositories/bank_account"
require "repositories/bank_account_transaction"

module Parsers
  class Desjardins
    # Given the contents of a CSV file from Desjardins, parse the contents into BankAccount and BankAccountTransaction objects.
    def call(rows)
      bank_accounts = rows.map do |row|
        next if row.map(&:strip).all?(&:empty?)

        account_num = row_to_account_num(row)
        last4 = if row[1].strip.empty? then
                  # VISA account
                  row[0][-4..-1]
                else
                  # Checking account
                  row[1][-4..-1]
                end
        Repositories::BankAccount.new(nil, account_num, last4)
      end

      transactions = rows.map do |row|
        next if row.map(&:strip).all?(&:empty?)

        check_num = row[6].strip.empty? ? nil : row[6].strip
        posted_on = Date.parse(row[3])
        amount = if row[1].strip.empty? then
                  # VISA account
                   row[11].strip.empty? ? BigDecimal(row[12]) : -1 * BigDecimal(row[11])
                 else
                   # Checking account
                   row[7].strip.empty? ? BigDecimal(row[8]) : -1 * BigDecimal(row[7])
                end

        account_num = row_to_account_num(row)
        Repositories::BankAccountTransaction.new(
          nil,
          account_num,
          check_num,
          posted_on,
          row[5],  # description1
          nil,     # description2
          amount
        )
      end

      [bank_accounts, transactions]
    end

    def row_to_account_num(row)
      Digest::SHA256.hexdigest(row[0, 3].map(&:strip).reject(&:empty?).join(" "))
    end
    private :row_to_account_num
  end
end
