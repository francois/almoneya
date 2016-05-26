require "spec_helper"
require "repositories/sign_in_repo"

RSpec.describe Repositories::SignInRepo do
  let(:now) { Time.now.utc }
  let(:sign_ins_ds) { double("sign_ins_ds") }
  let(:userpass_sign_ins_ds) { double("userpass_sign_ins_ds") }

  subject { Repositories::SignInRepo.new(sign_ins_dataset: sign_ins_ds, userpass_sign_ins_dataset: userpass_sign_ins_ds) }

  context "#create_username_password_authentication_attempt" do
    before(:each) do
      allow(sign_ins_ds).to receive(:returning).and_return(sign_ins_ds)
      allow(userpass_sign_ins_ds).to receive(:insert)
    end

    it "calls sign_ins_ds#insert with the correct parameters" do
      expect(sign_ins_ds).to receive(:insert).
        with(source_ip: "10.9.12.33", user_agent: nil, method: "userpass", successful: true).
        and_return([{sign_in_id: 2812, source_ip: "10.9.12.33", user_agent: nil, method: "userpass", successful: true, created_at: now}])
      subject.create_username_password_authentication_attempt(username: "francois", source_ip: "10.9.12.33", user_agent: nil, successful: true)
    end

    it "transforms the return value of the call to sign_ins_ds#insert to a SignIn object" do
      allow(sign_ins_ds).to receive(:insert).and_return([{sign_in_id: 17, source_ip: "47.11.9.129", user_agent: "Chrome 44.0 (Like Gecko)", method: "userpass", successful: true, created_at: now}])
      expect(subject.create_username_password_authentication_attempt(username: "francois", source_ip: "47.11.9.129", user_agent: "Chrome 44.0 (Like Gecko)", successful: true)).to eq(Repositories::SignIn.new(17, "47.11.9.129", "Chrome 44.0 (Like Gecko)", "userpass", true, now))
    end

    it "calls userpass_sign_ins_ds#insert with the sign_in_id and username" do
      allow(sign_ins_ds).to receive(:insert).and_return([{sign_in_id: 42, source_ip: "13.21.33.129", user_agent: "", method: "userpass", successful: true, created_at: now}])
      expect(userpass_sign_ins_ds).to receive(:insert).with(sign_in_id: 42, username: "francois")
      subject.create_username_password_authentication_attempt(username: "francois", source_ip: "13.21.33.129", user_agent: "", successful: true)
    end
  end
end
