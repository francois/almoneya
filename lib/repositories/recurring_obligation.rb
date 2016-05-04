module Repositories
  RecurringObligation = Struct.new(:recurring_obligation_id, :envelope, :description, :every, :period, :start_on, :end_on, :amount, :created_at, :updated_at) do
    alias_method :id, :recurring_obligation_id

    def envelope_name
      envelope.name
    end
  end
end
