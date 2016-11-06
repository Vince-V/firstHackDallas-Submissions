var express = require('express');
var router = express.Router();
var dburl = require('../conf/db_conf.json').url;
var MongoClient = require('mongodb').MongoClient;
var autoIncrement = require("mongodb-autoincrement");

/* GET users listing. */
router.get('/', function(req, res, next) {
  res.send('respond with a resource');
});

router.post('/addticket', function(req, res, next) {
  var info = {
      subject : req.body.subject,
      issuetype : req.body.issuetype,
      service : req.body.service,
      sid: req.body.sid,
      issue : req.body.issue,
      details : req.body.details,
      user : req.body.user,
      status : 1 // Ticket open
  }
  var response = {};
  MongoClient.connect(dburl, function(dberr, db){
    if(!dberr){
      autoIncrement.getNextSequence(db, 'ticketinfo', function (err, autoIndex) {
        var collection = db.collection('ticketinfo');
        info._id = autoIndex;                
        collection.insert(info);
        response.status = 1;
        response.msg = 'Ticket ' +info.subject + ' is successfully created.';
        response.ticketid = autoIndex;
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

router.get('/getticket', function(req, res, next) {  
  var query = {};  
  var response = {};
  MongoClient.connect(dburl, function(dberr, db){
    if(!dberr){
        var collection = db.collection('ticketinfo');
        collection.find(query).toArray(function(err, docs){          
            if(docs){
                res.send(JSON.stringify(docs));
            }
            else{
              response.status = 0;
              response.err = "No Ticket found";
              response.msg = "No Ticket found";
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

router.get('/getticket/:ticketid', function(req, res, next) {
  var ticketid = req.params.ticketid;
  var query = {};
  if(ticketid){
    query = { _id : Number(ticketid)};
  }
  var response = {};
  MongoClient.connect(dburl, function(dberr, db){
    if(!dberr){
        var collection = db.collection('ticketinfo');
        collection.find(query).toArray(function(err, docs){          
            if(docs){
                res.send(JSON.stringify(docs));
            }
            else{
              response.status = 0;
              response.err = "No Ticket found";
              response.msg = "No Ticket found";
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

router.get('/updateticket/:ticketid/:status', function(req, res, next) {
  var ticketid = req.params.ticketid;
  var status = Number(req.params.status);
  var findquery = { _id : Number(ticketid)};
  var updatequery = {$set:{status: status}};
  var response = {};
  MongoClient.connect(dburl, function(dberr, db){
    if(!dberr){
        var collection = db.collection('ticketinfo');
        collection.find(findquery, function(err, doc){
            if(doc){
                  collection.update(findquery, updatequery, function(err, doc1){
                  if(doc1){
                    response.status = 1;
                    response.msg = "Ticket updated succesfully";
                    res.send(JSON.stringify(response));
                  }
                  else
                  {
                    response.status = 0;
                    response.err = "Something went wrong";
                    response.msg = "Something went wrong";
                    res.send(JSON.stringify(response));
                  }
              });
            }
            else{
                    response.status = 0;
                    response.err = "Ticket does not exist";
                    response.msg = "Ticket does not exist";
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
