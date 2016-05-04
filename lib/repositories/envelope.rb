module Repositories
  Envelope = Struct.new(:envelope_id, :name, :created_at, :updated_at) do
    alias_method :id, :envelope_id
  end
end
