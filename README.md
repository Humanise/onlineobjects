# OnlineObjects

OnlineObjects is a webapp framework.

## Configuring

### Link up Humanise User Interface

The system is dependent on [Humanise User Interface](https://github.com/Humanise/hui)

```sh
cd src/main/webapp
ln -s path/to/hui hui
```

### Installing ImageMagick

ImageMagick is used to transform images

**Macports**

```
sudo port install ImageMagick
```

Uglify JS is used for JavaScript

```
sudo npm install uglify-js -g
```


### Developing

#### Compiling assets

```
npm install
grunt
```


#### Build docker image

```
docker build -t onlineobjects .
```

#### Run docker image

```
docker run -p 8080:8080 onlineobjects
```