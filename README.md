# 2018-group-11
Derek Chang, Idean Labib, Farrah Lin, Jessica Liu, Eddie Heredia

Calendue @JHU

Build Instructions
===================
1) `cd server`
2) `./build.sh`

Note: Run `./build.sh -t` to run integration tests. (Must have PostgreSQL environment variable JDBC_DATABASE_URL setup)
Note: Run `./build.sh --help` to see all options.

### To load the extension:
1) If you don't have `web-ext` installed yet, install with the following command:  
`npm install --global web-ext`
2) Navigate to extension root directory:  
`cd extension`
3) To start Firefox and load extension temporarily in browser:  
`web-ext run`
#### Or:
1) Open Firefox
2) Navigate to `about:debugging`
3) Load temporary extension:`/extension/manifest.json`
