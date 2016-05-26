module Repositories
  Goal = Struct.new(:goal_id, :envelope, :description, :due_on, :amount, :created_at, :updated_at) do
    alias_method :id, :goal_id

    def envelope_name
      envelope.name
    end
  end
end
