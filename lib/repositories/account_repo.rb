require "repositories/account"

module Repositories
  class AccountRepo
    def initialize(accounts_dataset:)
      @accounts_ds = accounts_dataset
    end

    attr_reader :accounts_ds
    private :accounts_ds

    def create(tenant_id:, account:)
      rows = accounts_ds.
        returning(:account_id, :account_code, :account_name, :account_kind, :created_at, :updated_at).
        insert(
          tenant_id: tenant_id,
          account_code: account.code,
          account_name: account.name,
          account_kind: account.kind)

      map_account(rows.first)
    end

    def find_all_for_tenant(tenant_id)
      ds = accounts_ds.
        filter(tenant_id: tenant_id).
        select(:account_id, :account_code, :account_name, :account_kind, :created_at, :updated_at)

      ds.map(&method(:map_account))
    end

    def map_account(row)
      Repositories::Account.new(
        row.fetch(:account_id),
        row.fetch(:account_code),
        row.fetch(:account_name),
        row.fetch(:account_kind),
        row.fetch(:created_at),
        row.fetch(:updated_at))
    end
    private :map_account
  end
end
