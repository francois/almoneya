module Repositories
  Obligation = Struct.new(:obligation_id, :envelope, :description, :every, :period, :start_on, :end_on, :amount, :created_at, :updated_at) do
    alias_method :id, :obligation_id

    def envelope_name
      envelope.name
    end
  end
end
