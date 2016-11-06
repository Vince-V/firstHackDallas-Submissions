var express = require('express');
var router = express.Router();
var dburl = require('../conf/db_conf.json').url;
var MongoClient = require('mongodb').MongoClient;

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});


// API to authenticate user
router.post('/authuser', function(req, res, next) {
  var info = {
      email : req.body.email,
      password : req.body.password
  };
  var response = {};
  MongoClient.connect(dburl, function(dberr, db){
      if(!dberr){
        var collection = db.collection('userinfo');
                
        collection.findOne({'email': info.email}, function(err, doc){
            if(err){
                response.status = 0;
                response.err = err;
                response.msg = "Something went wrong";
                res.send(JSON.stringify(response));
            }

            if(doc){
               if(doc.password == info.password){
                  response.status = 1; 
                  response.role = doc.role || 'user';                                   
                  response.msg = "Valid User";
                  res.send(JSON.stringify(response));
               }
               else{
                  response.status = 0;
                  response.err = "Wrong password";
                  response.msg = "Wrong password";
                  res.send(JSON.stringify(response));
               }
            }
            else
            {
                response.status = 0;
                response.err = "Wrong username or password";
                response.msg = "Wrong username or password";
                res.send(JSON.stringify(response));
            }
        });
      }
      else{
                response.status = 0;
                response.err = dberr;
                response.msg = "Something went wrong";
                res.send(JSON.stringify(response));
      }
  });
});

// Register user API
router.post('/registeruser', function(req, res, next) {
  var info = {
      email : req.body.email,
      password : req.body.password
  };

var response = {};
MongoClient.connect(dburl, function(dberr, db){
    if(!dberr){
      // success : 1, Error : 0, already exist :2
        var collection = db.collection('userinfo');
                
        collection.findOne({'email': info.email}, function(err, doc){          
          if(err){
            console.log(err);
            response.status = 0;
            response.error = err;
            response.msg = 'Something went wrong';
            res.send(JSON.stringify(response));
          }

          if(!doc){
            collection.insert(info);
            response.status = 1;
            response.msg = 'User ' +info.email + ' is successfully registered';
            res.send(JSON.stringify(response));
          }
          else
          {
            response.status = 2;            
            response.msg = 'User already exists';
            res.send(JSON.stringify(response));
          }          
        });
    }
    else
    {
      response.status = 0;
      response.error = dberr;
      response.msg = 'Something went wrong';
      res.send(JSON.stringify(response));
    }      
  });
});


// Add User details API
router.post('/adduserdetails', function(req, res, next) {
  var info = {
      name : req.body.name,
      dob : req.body.dob,
      disease : req.body.disease,
      ethnicity : req.body.ethnicity,
      faith : req.body.faith,
      location: req.body.location,
      subscription :[]
  };

  var email = req.body.email;

  var response = {};

  MongoClient.connect(dburl, function(dberr, db){
    if(!dberr){
        var collection = db.collection('userinfo');
        collection.findOne({'email': email}, function(err, doc){
            if(err){
              response.status = 0;
              response.error = err;
              response.msg = 'Something went wrong';
              res.send(JSON.stringify(response));
            }

            if(doc){
              collection.updateOne({'email': email}, {$set : info }, function(err1, doc1){
                  if(!err1){
                      response.status = 1;              
                      response.msg = 'Information saved successfully';
                      res.send(JSON.stringify(response));
                  }
                  else
                  {
                    response.status = 0;
                    response.error = err1;
                    response.msg = 'Something went wrong';
                    res.send(JSON.stringify(response));
                  }                  
              });              
            }
            else
            {
                    response.status = 0;
                    response.error = 'User ' + email + ' is not registered';
                    response.msg = 'Something went wrong';
                    res.send(JSON.stringify(response));
            }
        });       
    }
    else
    {
      response.status = 0;
      response.error = dberr;
      response.msg = 'Something went wrong';
      res.send(JSON.stringify(response));
    }        
  });

});


// Get user info API
router.get('/userinfo/:email', function(req, res, next) {
  var email = req.params.email;
  var response = {};

  MongoClient.connect(dburl, function(dberr, db){
    if(!dberr){
        var collection = db.collection('userinfo');
        collection.findOne({'email': email}, function(err, doc){
            if(err){
              response.status = 0;
              response.error = err;
              response.msg = 'Something went wrong';
              res.send(JSON.stringify(response));
            }

            if(doc){
              var result = JSON.stringify(doc);
              result = JSON.parse(result);
              delete result.password;
              res.send(JSON.stringify(result));
            }
            else
            {
              response.status = 0;
              response.error = 'User not found.';
              response.msg = 'Something went wrong';
              res.send(JSON.stringify(response));
            }
        });
    }
    else
    {
      response.status = 0;
      response.error = dberr;
      response.msg = 'Something went wrong';
      res.send(JSON.stringify(response));
    }   
  });

});



router.get('/subscribe/:email/:sid/', function(req, res, next) {
  var email = req.params.email;
  var sid = Number(req.params.sid);  
  var response = {};

  MongoClient.connect(dburl, function(dberr, db){
    if(!dberr){
        var collection = db.collection('userinfo');
        collection.findOne({'email': email}, function(err, doc){
          if(doc){
            var subs = doc.subscription || [];
            var serviceCollection =  db.collection('serviceinfo');
            serviceCollection.findOne({_id: sid}, function(err2, doc2){
                if(doc2){
                    subs.push({sid : sid, name: doc2.name});
                    collection.updateOne({email : email}, {$set:{subscription:subs}}, function(err1, doc1){
                    if(doc1){
                      response.status = 1;
                      response.msg = 'Subscription successfull';
                      res.send(JSON.stringify(response));
                    }
                    else{
                      response.status = 0;
                      response.err = "Something went wrong";
                      response.msg = "Something went wrong";
                      res.send(JSON.stringify(response));
                    }
                  });
                }
                else{
                  response.status = 0;
                  response.err = "Service not found";
                  response.msg = "Service not found";
                  res.send(JSON.stringify(response));
                }
                
            });
            
          }
          else{
                response.status = 0;
                response.err = "Something went wrong";
                response.msg = "Something went wrong";
                res.send(JSON.stringify(response));
          }
        });
    }
    else
    {
        response.status = 0;
        response.err = dberr;
        response.msg = "Something went wrong";
        res.send(JSON.stringify(response));
    }
  });
});


module.exports = router;
