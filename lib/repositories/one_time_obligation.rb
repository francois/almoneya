module Repositories
  OneTimeObligation = Struct.new(:one_time_obligation_id, :envelope, :description, :due_on, :amount, :created_at, :updated_at) do
    alias_method :id, :one_time_obligation_id

    def envelope_name
      envelope.name
    end
  end
end
