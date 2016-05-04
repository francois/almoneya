require "repositories/envelope"
require "repositories/one_time_obligation"

module Operations
  class CreateOneTimeObligation
    def initialize(obligation_repo:, envelope_repo:)
      @obligation_repo = obligation_repo
      @envelope_repo   = envelope_repo
    end

    attr_reader :obligation_repo, :envelope_repo
    private :obligation_repo, :envelope_repo

    def call(tenant_id, obligation)
      envelope = envelope_repo.create(tenant_id, obligation.envelope)
      new_obligation = obligation.dup
      new_obligation.envelope = envelope
      obligation_repo.create_one_time_obligation(tenant_id, new_obligation)
    end
  end
end
