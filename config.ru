$LOAD_PATH.unshift File.expand_path("../lib", __FILE__)
require_relative "lib/webui/app"

require "rack/contrib/locale"
require "i18n"

I18n.available_locales = %w(en fr fr-FR)
I18n.default_locale = "fr"

# Detects the user's locale and sets rack.locale in the environment
use Rack::Locale

# The application we're interested in running
run Webui::App
