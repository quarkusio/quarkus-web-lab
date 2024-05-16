To install `quarkus-reveal`:
```shell
jbang app install --fresh --force quarkus-reveal@ia3andy/quarkus-reveal
```

To start working or presenting (`-t quarkus` is to use the quarkus theme):
```shell
quarkus-reveal talk.md -t quarkus
```

```shell
# opens deck.md in the current directory (or the DEMO deck if not found)
quarkus-reveal -t quarkus
```


Add deck assets in `deck-assets/`, they will be accessible from your deck.

Use http://localhost:7979/?print-pdf to export a pdf.