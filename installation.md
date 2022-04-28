---
layout: default
title: Installation
nav_order: 5
has_children: false
permalink: installation
---

# Installation
{: .no_toc }
JGEA can be installed using Maven.

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## Cloning the repository

Clone JGEA on your local device:
```console
git clone https://github.com/ericmedvet/jgea.git
```

Proceed with the installation:
```console
mvn install
```

To include JGEA in your project edit its ``pom.xml`` adding the following dependency:
```xml
<dependencies>
        <dependency>
            <groupId>it.units.malelab</groupId>
            <artifactId>JGEA</artifactId>
            <version>2.0.2</version>
        </dependency>
</dependencies>
```

## Using the pre-packaged version

Download the [zip file](https://github.com/ericmedvet/jgea/releases/tag/v2.0.2) and place it in the ``libs`` folder of your project.

Edit the ``pom.xml`` of your project adding the following dependency:
```xml
<dependencies>
        <dependency>
            <groupId>it.units.malelab</groupId>
            <artifactId>JGEA</artifactId>
            <version>2.0.2</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/libs/JGEA.jar</systemPath>
        </dependency>
</dependencies>
```