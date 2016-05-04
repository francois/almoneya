require "repositories/one_time_obligation"

module Repositories
  class ObligationRepo
    def initialize(obligations_dataset:, one_time_obligations_dataset:, recurring_obligations_dataset:)
      @obligations_ds = obligations_dataset
      @one_time_obligations_ds = one_time_obligations_dataset
      @recurring_obligations_ds = recurring_obligations_dataset
    end

    attr_reader :obligations_ds, :one_time_obligations_ds, :recurring_obligations_ds
    private :obligations_ds, :one_time_obligations_ds, :recurring_obligations_ds

    def create_one_time_obligation(tenant_id, obligation)
      row0 = obligations_ds.
        returning(:obligation_id, :envelope_name, :description, :created_at, :updated_at).
        insert(
          tenant_id: tenant_id,
          envelope_name: obligation.envelope_name,
          description: obligation.description).first

      row1 = one_time_obligations_ds.
        returning(:one_time_obligation_id, :envelope_name, :due_on, :amount, :created_at, :updated_at).
        insert(tenant_id: tenant_id,
               envelope_name: obligation.envelope_name,
               due_on: obligation.due_on,
               amount: obligation.amount).first

      OneTimeObligation.new(row1.fetch(:one_time_obligation_id),
                            Envelope.new(nil, row1.fetch(:envelope_name)),
                            row0.fetch(:description),
                            row1.fetch(:due_on),
                            row1.fetch(:amount),
                            row1.fetch(:created_at),
                            row1.fetch(:updated_at))
    end

    def create_recurring_obligation(tenant_id, obligation)
      row0 = obligations_ds.
        returning(:obligation_id, :envelope_name, :description, :created_at, :updated_at).
        insert(
          tenant_id: tenant_id,
          envelope_name: obligation.envelope_name,
          description: obligation.description).first

        row1 = recurring_obligations_ds.
          returning(:recurring_obligation_id, :envelope_name, :every, :period, :start_on, :end_on, :amount, :created_at, :updated_at).
          insert(tenant_id: tenant_id,
                 envelope_name: obligation.envelope.name,
                 every: obligation.every,
                 period: obligation.period,
                 start_on: obligation.start_on,
                 end_on: obligation.end_on,
                 amount: obligation.amount).first

        RecurringObligation.new(
          row1.fetch(:recurring_obligation_id),
          Envelope.new(nil, row0.fetch(:envelope_name)),
          row0.fetch(:description),
          row1.fetch(:every),
          row1.fetch(:period),
          row1.fetch(:start_on),
          row1.fetch(:end_on),
          row1.fetch(:amount),
          row1.fetch(:created_at),
          row1.fetch(:updated_at))
    end
  end
end
