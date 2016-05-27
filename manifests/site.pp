# Always run apt-get update and upgrade before installing any package
exec{'/usr/bin/apt-get update':} -> exec{'/usr/bin/apt-get upgrade -y': timeout => 0} -> Package <| |>

package{[
  'build-essential',
  'byobu',
  'daemontools',
  'git',
  'heroku-toolbelt',
  'htop',
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

group{'francois':
  ensure => present,
}

user{'francois':
  ensure     => present,
  gid        => 'francois',
  groups     => ['sudo'],
  managehome => true,
  shell      => '/bin/zsh',
  require    => Package['zsh']
}

ssh_authorized_key{'francois@m481':
  ensure => present,
  type   => 'ssh-rsa',
  key    => 'AAAAB3NzaC1yc2EAAAADAQABAAABAQC2VWbsTL59eN/kOcVsps9QeFZQGpFqK6GU9cI/qRA+YUybQahdz+vW38kLyF2kcBPpIHI5lP/WnFL/UWqeHpM1wsOK3pQ8Aw9swV/3OnZ/4pLGkZoof+5fieyDiTe1Gdy2grBCyfEklVQmqLCMvGYix4Ka2IsyYYJu/lAEZk6lC/4ccPU7Gm42oWMjhysNGU6aguePe4xMVfoxVrCy9URzK+f5mQsxtTkdPTSB5aNIM6poCtbbIbrwOuALLvifN9etWdb4UWryIIKERxrJN1sUa77f5g+WN5YOhnJeHC0aLLrScDGMH6B6K+d7L0+4oOlWsCQ0eXQdfD/eqBtm/ZOJ',
  user   => 'francois',
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
  user    => 'francois',
  cwd     => '/home/francois',
  creates => '/home/francois/dotfiles/.git',
  require => [
    Package['git'],
    User['francois'],
  ],
}

exec{'use-zsh':
  command => '/usr/bin/chsh --shell /bin/zsh francois',
  unless  => '/bin/grep --quiet --extended-regexp "^francois:.*:/bin/zsh$" /etc/passwd',
  require => [
    Package['zsh'],
    User['francois'],
  ],
}

exec{'/usr/bin/cpan -i -f -T App::Sqitch DBD::Pg TAP::Parser::SourceHandler::pgTAP':
  user    => 'francois',
  creates => '/usr/local/bin/sqitch',
  require => [
    Exec['/usr/bin/apt-get update'],
    User['francois'],
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

file{'/home/francois/.config':
  ensure  => directory,
  owner   => 'francois',
  group   => 'francois',
  mode    => 0700,
  recurse => true,
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
  content => "# This file is managed by Puppet
# DO NOT EDIT

# DO NOT DISABLE!
# Database administrative login by Unix domain socket
local   all             postgres                                peer

local   all             all                                     peer
host    all             all             127.0.0.1/32            md5
host    all             all             ::1/128                 md5

# I'm lazy, this is fine for development
host    vagrant         vagrant         10.9.1.1/32             trust
"
}

file{'/etc/postgresql/9.4/main/postgresql.conf':
  ensure  => file,
  owner   => postgres,
  group   => postgres,
  notify  => Service['postgresql'],
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
