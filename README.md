# quarkus-web-lab

![qr-code](https://github.com/quarkusio/quarkus-web-lab/assets/6836179/91f45d34-a0c0-4093-a3e2-2428c66f9d23)

**github.com/quarkusio/quarkus-web-lab**

This lab contains 3 parts to do in order.

### 1 - The CMS (~60m)

Here we allow bloggers to manage blog entries. [Read more...](./1-cms/README.md)

### 2 - The Blog (~30m)

Here we show the blog created by the CMS. [Read more...](./2-blog/README.md)

### 3- The Comments (~30m)

Here we allow users to add comments on the blog. [Read more...](./3-comments/README.md)

## Getting your environment ready

To get the most out of this lab, make sure you have the following working on your workstation:

- JDK (17+)
- Git
- Java IDE
- (Optional) Quarkus IDE Tooling: https://quarkus.io/guides/ide-tooling

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

### Git (Optional)

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
