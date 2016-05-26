require "spec_helper"
require "repositories/sign_in"

RSpec.describe Repositories::SignIn do
  let(:now) { Time.now.utc }
  subject { Repositories::SignIn.new(12, "127.0.0.1", "Google Chrome 44.0/...", "userpass", now) }

  it { expect(subject).to eq(subject) }
  it { expect(subject).to eq(Repositories::SignIn.new(12, "127.0.0.1", "Google Chrome 44.0/...", "userpass", now)) }
  it { expect(subject.id).to eq(subject.sign_in_id) }
end
