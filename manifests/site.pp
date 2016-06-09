# Always run apt-get update and upgrade before installing any package
exec{'/usr/bin/apt-get update':} -> exec{'/usr/bin/apt-get upgrade -y': timeout => 0} -> Package <| |>

package{[
  'build-essential',
  'byobu',
  'daemontools',
  'git',
  'heroku-toolbelt',
  'htop',
  'jq',
  'libpq5',
  'libpq-dev',
  'libreadline-dev',
  'ntp',
  'openjdk-8-jdk',
  'postgresql-9.4',
  'postgresql-client-9.4',
  'postgresql-contrib-9.4',
  'postgresql-server-dev-9.4',
  'python-setuptools',
  'unzip',
  'vim-nox',
  'wget',
  'zsh',
]:
  ensure => latest,
}

package{[
  'nodejs',
]:
  ensure => absent,
}

file{'/usr/local/bin/edb':
  ensure  => file,
  mode    => 0775,
  content => '#!/bin/sh
exec bundle exec "${@}"',
}

file{'/etc/apt/sources.list.d/pgdg.list':
  ensure  => file,
  content => 'deb http://apt.postgresql.org/pub/repos/apt/ trusty-pgdg main
',
}

exec{'/usr/bin/wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | /usr/bin/apt-key add -':
  creates => '/etc/apt/trusted.gpg.d/apt.postgresql.org.gpg',
}

File['/etc/apt/sources.list.d/pgdg.list'] -> Exec['/usr/bin/wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | /usr/bin/apt-key add -'] -> Exec['/usr/bin/apt-get update']

exec{'/usr/bin/git clone git://github.com/francois/dotfiles.git':
  user    => 'vagrant',
  cwd     => '/home/vagrant',
  creates => '/home/vagrant/dotfiles/.git',
  require => [
    Package['git'],
  ],
}

exec{'/usr/bin/cpan -i -f -T App::Sqitch DBD::Pg TAP::Parser::SourceHandler::pgTAP':
  timeout => 1800, # Wait up to 30 minutes for Sqitch and dependencies to install
  creates => '/usr/local/bin/sqitch',
  require => [
    Exec['/usr/bin/apt-get update'],
  ],
}

exec{'download pgxn client':
  command => '/usr/bin/wget -O /usr/local/src/pgxnclient-1.2.1.tar.gz https://pypi.python.org/packages/source/p/pgxnclient/pgxnclient-1.2.1.tar.gz',
  creates => '/usr/local/src/pgxnclient-1.2.1.tar.gz',
  require => Package['wget'],
}

exec{'extract pgxn client':
  command => '/bin/tar xzf /usr/local/src/pgxnclient-1.2.1.tar.gz',
  cwd     => '/usr/local/src',
  creates => '/usr/local/src/pgxnclient-1.2.1/setup.py',
  require => Exec['download pgxn client'],
}

exec{'install pgxn client':
  command => '/usr/bin/python /usr/local/src/pgxnclient-1.2.1/setup.py build && /usr/bin/python /usr/local/src/pgxnclient-1.2.1/setup.py install',
  cwd     => '/usr/local/src/pgxnclient-1.2.1',
  creates => '/usr/local/bin/pgxn',
  require => [
    Package['python-setuptools'],
    Exec['extract pgxn client'],
  ],
}

exec{'install pgtap':
  command => '/usr/local/bin/pgxn install pgtap',
  require => [
    Exec['install pgxn client'],
  ],
}

file{'/etc/apt/sources.list.d/heroku.list':
  ensure  => file,
  content => 'deb http://toolbelt.heroku.com/ubuntu ./
',
}

exec{'/usr/bin/wget --quiet -O - https://toolbelt.heroku.com/apt/release.key | /usr/bin/apt-key add -':
  # creates => '/etc/apt/trusted.gpg.d/apt.postgresql.org.gpg',
}

File['/etc/apt/sources.list.d/heroku.list'] -> Exec['/usr/bin/wget --quiet -O - https://toolbelt.heroku.com/apt/release.key | /usr/bin/apt-key add -'] -> Exec['/usr/bin/apt-get update']

$node_version = "4.4.3"
exec{"download nodejs ${node_version}":
  command => "/usr/bin/wget https://nodejs.org/dist/v${node_version}/node-v${node_version}-linux-x64.tar.xz",
  creates => "/usr/local/src/node-v${node_version}-linux-x64.tar.xz",
  cwd     => '/usr/local/src',
  require => Package['wget'],
} -> exec{"install nodejs ${node_version}":
  command => "/bin/tar -xf /usr/local/src/node-v${node_version}-linux-x64.tar.xz",
  cwd     => '/usr/local',
  creates => "/usr/local/node-v${node_version}/bin/node"
} -> file{'/usr/local/bin/node':
  ensure => link,
  target => "/usr/local/node-v${node_version}-linux-x64/bin/node"
} -> file{'/usr/local/bin/npm':
  ensure => link,
  target => "/usr/local/node-v${node_version}-linux-x64/bin/npm"
} -> exec{'install webpack':
  command => '/usr/local/bin/npm install -g webpack',
  creates => "/usr/local/node-${node_version}-linux-x64/bin/webpack",
  cwd     => '/usr/local'
} -> file{'/usr/local/bin/webpack':
  ensure => link,
  target => "/usr/local/node-v${node_version}-linux-x64/bin/webpack",
}

service{'postgresql':
  ensure  => running,
  enable  => true,
  require => Package['postgresql-9.4'],
}

file{'/etc/postgresql/9.4/main/pg_hba.conf':
  ensure  => file,
  owner   => postgres,
  group   => postgres,
  notify  => Service['postgresql'],
  require => Package['postgresql-9.4'],
  content => "# This file is managed by Puppet
# DO NOT EDIT

# DO NOT DISABLE!
# Database administrative login by Unix domain socket
local   all             postgres                                peer

local   all             all                                     peer
host    all             all             127.0.0.1/32            md5
host    all             all             ::1/128                 md5

# I'm lazy, this is fine for development
host    vagrant         vagrant         10.9.1.0/8              trust
"
}

file{'/etc/postgresql/9.4/main/postgresql.conf':
  ensure  => file,
  owner   => postgres,
  group   => postgres,
  notify  => Service['postgresql'],
  require => Package['postgresql-9.4'],
  content => "# This file is managed by Puppet
# DO NOT EDIT

data_directory = '/var/lib/postgresql/9.4/main'
hba_file = '/etc/postgresql/9.4/main/pg_hba.conf'
ident_file = '/etc/postgresql/9.4/main/pg_ident.conf'
external_pid_file = '/var/run/postgresql/9.4-main.pid'

# This exposes the PostgreSQL outside the VirtualBox VM
listen_addresses = '0.0.0.0'
port = 5432

max_connections = 20
unix_socket_directories = '/var/run/postgresql'
shared_buffers = 128MB
work_mem = 16MB
log_line_prefix = '%t [%p-%l] %q%u@%d '
timezone = 'Etc/UTC'
"
}

exec{'/usr/bin/wget --quiet https://github.com/avh4/elm-format/releases/download/0.3.1-alpha/elm-format-0.17-0.3.1-alpha-linux-x64.tgz':
  cwd     => '/usr/local/src',
  creates => '/usr/local/src/elm-format-0.17-0.3.1-alpha-linux-x64.tgz',
} -> exec{'/bin/tar -xzf /usr/local/src/elm-format-0.17-0.3.1-alpha-linux-x64.tgz':
  cwd     => '/usr/local/bin',
  creates => '/usr/local/bin/elm-format',
}
