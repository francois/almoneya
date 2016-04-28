$LOAD_PATH.unshift File.expand_path("../lib", __FILE__)
require_relative "lib/webui/app"

require "rack/contrib/locale"
require "i18n"

I18n.available_locales = %w(en fr fr-FR)
I18n.default_locale = "fr"

use Rack::Session::Cookie, :key => "acctsoft_session",
                           :path => "/",
                           :expire_after => 86400, # 1 day in seconds
                           :secret => ENV["SESSION_SECRET"] || SecureRandom.hex(64)


# Detects the user's locale and sets rack.locale in the environment
use Rack::Locale

# The application we're interested in running
run Webui::App
