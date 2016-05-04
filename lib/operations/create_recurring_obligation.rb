require "repositories/recurring_obligation"

module Operations
  class CreateRecurringObligation
    def initialize(envelope_repo:, obligation_repo:)
      @envelope_repo = envelope_repo
      @obligation_repo = obligation_repo
    end

    attr_reader :envelope_repo, :obligation_repo
    private :envelope_repo, :obligation_repo

    def call(tenant_id, obligation)
      envelope = envelope_repo.create(tenant_id, obligation.envelope)
      new_obligation = obligation.dup
      new_obligation.envelope = envelope
      obligation_repo.create_recurring_obligation(tenant_id, obligation)
    end
  end
end
