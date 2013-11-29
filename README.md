README
======

This diploma thesis can be build with rake, lualatex, and biber. A Vagrantfile and an Ansible playbook are provided in the `vm` directory. These will set up a Virtual Box VM with all necessary components to build this thesis.

Setup
-----

1. Install [Virtual Box](http://www.vagrantup.com/)
2. Install [Vagrant](https://www.virtualbox.org/)
3. run the following command: `vagrant plugin install vagrant-vbguest`

Usage
-----

(all commands have to be run inside the directory that contains the `Vagrantfile`)

To create and provision a VM, run:

    vagrant up

To connect to the VM via ssh, run:

    vagrant ssh

(The ssh executable must be inside `PATH`)

The thesis source files are located in `/thesis`. Execute the following to change into this directory:

    cd /thesis

Now you can run the rake task by executing:

    rake [taskname]

To build a pdf version of this thesis, run:

    rake build

To see all available tasks, run:

    rake

To stop the VM, run:

    vagrant halt

To destroy the VM, run:

    vagrant destroy