HesseBench - The Enterprise Streaming Benchmark (ESB)
---
This repository contains *HesseBench*, the enterprise streaming benchmark. It allows comparing [data stream processing systems](https://en.wikipedia.org/wiki/Stream_processing) (DSPSs) and architectures in an enterprise context, i.e. in an environemnt where streaming data is integrated with existing, historical business data.
This repository contains the HesseBench toolkit, example configurations, and an example query implementation using [Apache Beam](https://github.com/apache/beam).

For further details, see:
*Hesse, Guenter, et al. "HesseBench: The Enterprise Streaming Benchmark" 2020, tbd.*

#### Table of Contents  
[1. HesseBench Architecture](#1-hessebench-architecture)<br/>
[2. HesseBench Process](#2-hessebench-process)<br/>
[3. Structure of the Project](#3-structure-of-the-project)<br/>
[4. HesseBench Setup and Execution](#4-hessebench-setup)<br/>
[5. HesseBench Results](#5-hessebench-results)

## 1. HesseBench Architecture <a name="1-hessebench-architecture"/>
The overall architecture is visualized in the image below:
<p align="center">
<img src="https://owncloud.hpi.de/index.php/apps/files_sharing/ajax/publicpreview.php?x=2378&y=848&a=true&file=HesseBenchArch.png&t=piRX6IDNo5Gy9bp&scalingup=0" width="600">
</p>

Input data is sent to [Apache Kafka](https://kafka.apache.org/) by the data sender tool, which is [part of this repository](https://github.com/Gnni/EnterpriseStreamingBenchmark/tree/master/tools/datasender).
The DSPS runs the queries. It gets the data from Apache Kafka as input as well as from the enterprise database management system (DBMS) when required by the query.
After the configured time of the benchmark run is over, the validator and result calculator, which are also part of this repository, can check the query result correctness and compute the benchmark results, i.e., the latencies.

## 2. HesseBench Process <a name="2-hessebench-process"/>

A brief process overview of HesseBench is visualized in the activity diagram below:
<p align="center">
<img src="https://owncloud.hpi.de/index.php/apps/files_sharing/ajax/publicpreview.php?x=2378&y=848&a=true&file=Screenshot%25202020-02-19%2520at%252014.13.11.png&t=gBJplJAIye1dvy0&scalingup=0" width="900">
</p>

The entire process is automated using [Ansible](https://www.ansible.com/) scripts.

## 3. Structure of the Project <a name="3-structure-of-the-project"/>

```
EnterpriseStreamingBenchmark
│   README.md
│   .gitignore
│   .gitlab-ci.yml    
│   build.sbt
│   scalastyle-config.xml
│
└───ci
│   │   Dockerfile
│
└───implementation
│   └───beam
│       └───...
│   
└───project
│   │   build.properties
│   │   Dependencies.scala
│
└───tools
    └───commons
    │   └───src
    │       └───...
    │       │   commons.conf
    │
    └───configuration
    │   └───group_vars
    │   └───plays
    │   └───roles
    │   └───...
    │   │   ansible.cfg
    │   │   hosts
    │
    └───datasender
    │   └───src
    │       └───...
    │       │   datasender.conf
    │
    └───tpc-c_gen
    │   └───src
    │       └───...
    │       │   tpc-c.properties
    │
    └───util
    │   └───...
    │
    └───validator
        └───...
```

## 4. HesseBench Setup and Execution <a name="4-hessebench-setup"/>
All steps are tested on Ubuntu servers.

- Create a user `benchmarker` on all involved machines that has sudo access
- Ensure that this user can connect to all machines via ssh w/o password, e.g., through adding the ssh public key to the `authorized_keys` files
- Install `Apache Kafka` and the DSPSs to be tested under `/opt/` - you can find example configurations in the `tools/configurations` directory
- Make Apache Kafka a service:
  - Run `sudo apt install policykit-1`
  - Copy `tools/configuration/kafka/etc/init.d/kafka` to `/etc/init.d/` on the Apache Kafka servers
  - Run `update-rc.d kafka defaults`
- Install `PostgreSQL` on one server (you can use another DBMS, however, that requires some adaptions in the tools)
- Create the directory `Benchmarks` ind the home directory of the user `benchmarker` and clone the repository into this folder
- The project can be built using `sbt assembly`

###### tools/commons/commons.conf
- Define the Apache Kafka topic prefix and the benchmark run number, which will have an impact on the Apache Kafka topic names that are going to be created by the ansible scripts
-  Define the query you want to execute (config for each query in comment)
- Define the sending interval, which determines the pause between sending two records - a pause of, e.g., 1,000,000ns would result in an input rate of 1,000 records/s
- Define the benchmark duration
- Define the Apache Kafka bootstrap servers and zookeeper servers

###### tools/configuration
- After cloning the repository and setting up the systems, you change to the `tools/configuration` directory and start the benchmark (as shown in the activity diagram above) from here, e.g., via `ansible-playbook -vvvv plays/benchmark-runner-beam.yml` for the example implementations of this repository. The number of `v` define the level of verbosity
- Adapt the `group_vars/all` files if needed
- The directories `plays` and `roles` contain several ansible files, which can be adapted if needed. The starting point that represents the entire process is `plays/benchmark-runner-beam.yml` for the example implementation. These scripts also contain information about, e.g., how to start the data sender or the data generation.
- The `hosts` file needs to be edited, i.e, the servers' IP addresses needs to be entered

###### tools/datasender
- Input data is taken from DEBS 2012 Grand Challenge, which can be downloaded from [ftp://ftp.mi.fu-berlin.de/pub/debs2012/](ftp://ftp.mi.fu-berlin.de/pub/debs2012/)
- This data file needs converted using the `dos2unix` command, and duplicated, so that there are two input files.
- The two files needs to be extended by a machine ID using the following commands (adapt file names):
  - First file: `awk 'BEGIN { FS = OFS = "\t" } { $(NF+1) = 1; print $0 }' input1.csv >
 output1.csv`
  - Second file: `awk 'BEGIN { FS = OFS = "\t" } { $(NF+1) = 2; print $0 }' input2.csv >
output2.csv`
- A third input file is `production_times.csv`, which is generated by the TPC-C data generator that is part of this project.
- The file `datasender.conf` contains Apache Kafka producer configs and the location of the input data files.
- The file `src/main/resources/application.conf` needs the correct DBMS configuration.

###### tools/tpc-c_gen
The `tpc-c.properties` file contains the default setting WRT the number of warehouses and the data output directory. Changes of the output directory require according adaptions in ansible scripts.

###### tools/validator
The file `src/main/resources/application.conf` needs the correct DBMS configuration.

###### Example Implementation
If you want to use the example implementation, you need to adapt at least two files accordingly:
- `implementation/beam/src/main/resources/beam.properties`
- The `beamRunner` variable in `build.sbt`

## 5. HesseBench Results <a name="5-hessebench-results"/>
The validator will create a `logs` directory that will contain information about the, e.g., query result correctness, and latencies.
