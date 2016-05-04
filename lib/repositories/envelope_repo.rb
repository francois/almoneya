require "repositories/envelope"

module Repositories
  class EnvelopeRepo
    def initialize(envelopes_dataset:)
      @envelopes_ds = envelopes_dataset
    end

    attr_reader :envelopes_ds
    private :envelopes_ds

    def create(tenant_id, envelope)
      row = envelopes_ds.
        returning(:envelope_id, :envelope_name, :created_at, :updated_at).
        insert(tenant_id: tenant_id, envelope_name: envelope.name).first

      Envelope.new(row.fetch(:envelope_id),
                   row.fetch(:envelope_name),
                   row.fetch(:created_at),
                   row.fetch(:updated_at))
    end
  end
end
