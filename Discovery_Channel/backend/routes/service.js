var express = require('express');
var router = express.Router();
var dburl = require('../conf/db_conf.json').url;
var MongoClient = require('mongodb').MongoClient;
var autoIncrement = require("mongodb-autoincrement");

router.get('/', function(req, res, next) {
  res.render('This is service page');
});

// Get service info API
router.get('/serviceinfo', function(req, res, next) {
  var response = {};
  MongoClient.connect(dburl, function(dberr, db){
    if(!dberr){
      
        var collection = db.collection('serviceinfo');

        collection.find().toArray(function(err,docs){
            if(err){
                response.status = 0;
                response.error = err;
                response.msg = "Something went wrong";

                res.send(JSON.stringify(response));
            }

            if(docs){
                res.send(JSON.stringify(docs));
            }
            else
            {
                response.status = 1;
                response.error = 'No service found';
                response.msg = 'No service found';
                res.send(JSON.stringify(response));
            }            
        });
    }
    else
    {
        response.status = 0;
        response.error = dberr;
        response.msg = "Something went wrong";
        res.send(JSON.stringify(response));
    }
    
  });
});

router.get('/serviceinfo/:search', function(req, res, next) {
  var response = {};
  var search = req.params.search;
  MongoClient.connect(dburl, function(dberr, db){
    if(!dberr){
      
        var collection = db.collection('serviceinfo');

        collection.find({$text:{$search:search}}).toArray(function(err,docs){
            if(err){
                response.status = 0;
                response.error = err;
                response.msg = "Something went wrong";

                res.send(JSON.stringify(response));
            }

            if(docs){
                res.send(JSON.stringify(docs));
            }
            else
            {
                response.status = 0;
                response.error = 'No service found';
                response.msg = 'No service found';
                res.send(JSON.stringify(response));
            }            
        });
    }
    else
    {
        response.status = 0;
        response.error = dberr;
        response.msg = "Something went wrong";
        res.send(JSON.stringify(response));
    }
    
  });
});

router.post('/addservice', function(req, res, next) {
  var info = {
      name : req.body.name,
      image : req.body.image,
      description : req.body.description,
      location : req.body.location,
      contact : req.body.contact,
      category : req.body.category,
      disease : req.body.disease
  }
  var response = {};
  MongoClient.connect(dburl, function(dberr, db){
    if(!dberr){
      autoIncrement.getNextSequence(db, 'serviceinfo', function (err, autoIndex) {
        var collection = db.collection('serviceinfo');
        info._id = autoIndex;
        info.isactive = 1;        
        collection.insert(info);
        response.status = 1;
        response.msg = 'Service ' +info.name + ' is successfully registered';
        res.send(JSON.stringify(response));
      });        
    }
    else
    {
        response.status = 0;
        response.error = dberr;
        response.msg = "Something went wrong";
        res.send(JSON.stringify(response));
    }
    
  });
});

router.get('/recommendation/:email', function(req, res, next) {
  var response = {};
  var email = req.params.email;
  MongoClient.connect(dburl, function(dberr, db){
    if(!dberr){      
        var collection = db.collection('serviceinfo');
        var usercollection = db.collection('userinfo');
        usercollection.findOne({email: email}, function(err, doc){
            if(doc){
                var str =  doc.location + ' ' + doc.ethnicity + ' ' + doc.disease;
                //res.send(str);
                collection.find({$text:{$search:str}}).toArray(function(err,docs){
                    if(err){
                        response.status = 0;
                        response.error = err;
                        response.msg = "Something went wrong";

                        res.send(JSON.stringify(response));
                    }

                    if(docs){
                        res.send(JSON.stringify(docs));
                    }
                    else
                    {
                        response.status = 0;
                        response.error = 'No service found';
                        response.msg = 'No service found';
                        res.send(JSON.stringify(response));
                    }            
                });
            }
            else
            {
                response.status = 0;
                response.error = 'User not found';
                response.msg = 'User not found';
                res.send(JSON.stringify(response));
            }
        });            
    }
    else
    {
        response.status = 0;
        response.error = dberr;
        response.msg = "Something went wrong";
        res.send(JSON.stringify(response));
    }
    
  });
});

module.exports = router;