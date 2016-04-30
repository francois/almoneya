require "digest/sha2"
require "repositories/bank_account"
require "repositories/bank_account_transaction"

module Parsers
  class RBC
    # Given the contents of a CSV file from RBC, parse the contents into BankAccount and BankAccountTransaction objects.
    def call(rows)
      bank_accounts = rows.map do |row|
        next if row[0] == "Type de compte"

        account_num = Digest::SHA256.hexdigest(row[0, 2].join(" "))
        last4       = row[1][-4..-1]
        Repositories::BankAccount.new(nil, account_num, last4)
      end

      transactions = rows.map do |row|
        next if row[0] == "Type de compte"

        posted_on_components = row[2].split(/\D/)
        posted_on = Date.new(posted_on_components[2].to_i, posted_on_components[0].to_i, posted_on_components[1].to_i)

        account_num = Digest::SHA256.hexdigest(row[0, 2].join(" "))
        Repositories::BankAccountTransaction.new(
          nil,
          account_num,
          row[3],             # check_number
          posted_on,
          row[4],             # description1
          row[5],             # description2
          BigDecimal(row[6])) # amount
      end

      [bank_accounts, transactions]
    end
  end
end
