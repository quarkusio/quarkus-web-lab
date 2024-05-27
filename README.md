# quarkus-web-lab

This lab contains 3 parts

## The CMS

Here we allow an blogger to create new blog entries. [Read more...](./1-cms/README.md)

## The Blog

Here we show the blog created by the CMS. [Read more...](./2-blog/README.md)

## The Comments

Here we allow users to add comments on the blog. [Read more...](./3-comments/README.md)


## Getting your environment ready

To get the most out of this lab, make sure you have the following working on your workstation:

- JDK (17+)
- Git
- IDE

### JDK

Choose any JDK, example:

- [Red Hat OpenJDK](https://developers.redhat.com/products/openjdk/download)
- [Oracle OpenJDK](https://www.oracle.com/java/technologies/downloads/)

After you installed the JDK, make sure java is available on your Path, example:

 ```sh
$ java -version
openjdk version "21.0.3" 2024-04-16
OpenJDK Runtime Environment (Red_Hat-21.0.3.0.9-1) (build 21.0.3+9)
OpenJDK 64-Bit Server VM (Red_Hat-21.0.3.0.9-1) (build 21.0.3+9, mixed mode, sharing)
 ```

### Git

Install Git (to get the code from the lab, alternatively, you can download the starter project as a zip from GitHub)

- [Git](https://git-scm.com/downloads)

You can test your git by checking the version:

 ```sh
 $ git -v
git version 2.45.0
 ```

### IDE

You can use any IDE you want, example:

- [IntelliJ](https://www.jetbrains.com/idea/)
- [Eclipse](https://eclipseide.org/)
- [VS Code](https://code.visualstudio.com/)
- [Netbeans](https://netbeans.apache.org)

### Getting the code:

To get started with the code, checkout the lab using Git:

 ```sh
 git clone git@github.com:quarkusio/quarkus-web-lab.git
 cd quarkus-web-lab
 ```
