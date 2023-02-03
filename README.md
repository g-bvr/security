# Repository g-bvr/security

This repository defines a plugin that can be used to enhance the built-in set of commands
to encrypt and decrypt secret information using an asymmetric (aka. public/private) master key pair.

## Activation

This plugin can be integrated into the [core docker image](https://hub.docker.com/r/gitbeaver/core/tags)
by executing the following beaver script:

```
GIT CLONE https://github.com/g-bvr security main
PLUGIN COMPILE security/src/main/java
PLUGIN ENABLE org.jkube.gitbeaver.SecurityPlugin
```

A more convenient way to build a gitbeaver release with multiple
plugins (based on a tabular selection)
is provided by E. Breuninger GmbH & Co. in the public repository
[e-breuninger/git-beaver](https://github.com/e-breuninger/git-beaver).

## Documentation of defined commands

A list of all commands defined by this plugin can be found in this [automatically generated documentation](https://htmlpreview.github.io/?https://raw.githubusercontent.com/g-bvr/security/main/doc/SecurityPlugin.html). 

## Initialization

It initialization of this plugin the file ```/masterkey``` is read. If the file exists, is not empty and is not equal to ```not-set```,
then the master key is read from that file. The file is then deleted to prevent other code from reading it. 
Thus this plugin should be enabled as early as possible in the plugin setup sequence.

## Hints for operating on GCP

The recommended way to store the master key is to use the GCP SecretManager. The master key stored as a GCP secret can by passed to the gitbeaver container as environment variable.
It is best practice to decouple the creation of the master key secret from individual user operation: The user who triggers the creation 
of the master key (once, for setup of the gitbeaver ecosystem) does not need any SecretManager permissions to do so.

A convenient way how this can be realized (using the [web-server plugin](https://github.com/g-bvr/web-server)) is 
illustrated using [these beaver scripts](https://github.com/e-breuninger/git-beaver-gcp/tree/main/gitbeaver) in the public repository
[e-breuninger/git-beaver-gcp](https://github.com/e-breuninger/git-beaver-gcp) (kindly provided by E. Breuninger GmbH & Co.).
