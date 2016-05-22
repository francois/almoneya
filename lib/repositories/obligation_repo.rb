require "repositories/envelope"
require "repositories/goal"
require "repositories/obligation"

module Repositories
  class ObligationRepo
    def initialize(goals_dataset:, obligations_dataset:)
      @goals_ds       = goals_dataset
      @obligations_ds = obligations_dataset
    end

    attr_reader :obligations_ds, :goals_ds, :obligations_ds
    private :obligations_ds, :goals_ds, :obligations_ds

    def create_goal(tenant_id, goal)
      row = goals_ds.
        returning(:goal_id, :envelope_name, :description,  :due_on, :amount, :created_at, :updated_at).
        insert(tenant_id: tenant_id,
               envelope_name: goal.envelope_name,
               due_on: goal.due_on,
               amount: goal.amount).first

      Goal.new(row.fetch(:goal_id),
               Envelope.new(nil, row.fetch(:envelope_name)),
               row.fetch(:description),
               row.fetch(:due_on),
               row.fetch(:amount),
               row.fetch(:created_at),
               row.fetch(:updated_at))
    end

    def create_obligation(tenant_id, obligation)
      row = obligations_ds.
        returning(:obligation_id, :envelope_name, :description, :every, :period, :start_on, :end_on, :amount, :created_at, :updated_at).
        insert(tenant_id: tenant_id,
               envelope_name: obligation.envelope.name,
               every: obligation.every,
               period: obligation.period,
               start_on: obligation.start_on,
               end_on: obligation.end_on,
               amount: obligation.amount).first

      Obligation.new(
        row.fetch(:obligation_id),
        Envelope.new(nil, row.fetch(:envelope_name)),
        row.fetch(:description),
        row.fetch(:every),
        row.fetch(:period),
        row.fetch(:start_on),
        row.fetch(:end_on),
        row.fetch(:amount),
        row.fetch(:created_at),
        row.fetch(:updated_at))
    end
  end
end
