var assert = require('assert');
let chai = require('chai');
let chaiHttp = require('chai-http');
let should = chai.should();

var expect = chai.expect;

var chaiAsPromised = require('chai-as-promised');
chai.use(chaiAsPromised);

chai.use(chaiHttp);

var Chance = require('chance'),
  chance = new Chance();

var serverAddress = "http://localhost:9000";

/**
 * The purpose of these tests are primarily to ensure proper visibility checks
 * A private entity should not be accessible if the requester isn't owner of the entity
 * These tests also ensure that no entity is saved if incomplete payloads are given
 */
var correctprofile
describe('/POST profile', () => {
  it("Creating a profile should work", function () {
    correctprofile = chai.request(serverAddress)
      .post("/profile").send({
        "firstname": "John",
        "lastname": "Doe",
        "gender": "female",
        "birthDay": "1976-10-30",
        "deathDay": "",
        "profilePicture": "http://www.wikiality.com/file/2016/11/bears1.jpg",
        "born": {
          "name": "born",
          "description": "Jane is born",
          "location": {
            "city": "New Yorkus",
            "province": "New York",
            "country": "U.S.A"
          },
          "media": [
            {
              "type": "image",
              "path": "http://www.rd.com/wp-content/uploads/sites/2/2016/02/06-train-cat-shake-hands.jpg"
            }
          ]
        }
      })
    return correctprofile.then(function (response) {
      correctprofile = response.body;
      expect(response.status).to.equal(200)
    })
  })
})
describe('/POST profile', () => {
  it("Creating an incomplete profile (missing firstname) should not work", function () {
    var result = chai.request(serverAddress)
      .post("/profile").send({
        "lastname": "Doe",
        "gender": "female",
        "birthDay": "1976-10-30",
        "deathDay": "",
        "profilePicture": "http://www.wikiality.com/file/2016/11/bears1.jpg",
        "born": {
          "name": "born",
          "description": "Jane is born",
          "location": {
            "city": "New Yorkus",
            "province": "New York",
            "country": "U.S.A"
          },
          "media": [
            {
              "type": "image",
              "path": "http://www.rd.com/wp-content/uploads/sites/2/2016/02/06-train-cat-shake-hands.jpg"
            }
          ]
        }
      })
    return expect(result).to.be.rejected
  })
})

describe('/POST profile', () => {
  it("Creating an incomplete profile (missing born.name) should not work", function () {
    var result = chai.request(serverAddress)
      .post("/profile").send({
        "firstname": "Jane",
        "lastname": "Doe",
        "gender": "female",
        "birthDay": "1976-10-30",
        "deathDay": "",
        "profilePicture": "http://www.wikiality.com/file/2016/11/bears1.jpg",
        "born": {
          "description": "Jane is born",
          "location": {
            "city": "New Yorkus",
            "province": "New York",
            "country": "U.S.A"
          },
          "media": [
            {
              "type": "image",
              "path": "http://www.rd.com/wp-content/uploads/sites/2/2016/02/06-train-cat-shake-hands.jpg"
            }
          ]
        }
      })
    return expect(result).to.be.rejected
  })
})

describe('/POST profile', () => {
  it("Creating an incomplete profile (missing born.location.city) should not work", function () {
    var result = chai.request(serverAddress)
      .post("/profile").send({
        "firstname": "Jane",
        "lastname": "Doe",
        "gender": "female",
        "birthDay": "1976-10-30",
        "deathDay": "",
        "profilePicture": "http://www.wikiality.com/file/2016/11/bears1.jpg",
        "born": {
          "name": "born",
          "description": "Jane is born",
          "location": {
            "province": "New York",
            "country": "U.S.A"
          },
          "media": [
            {
              "type": "image",
              "path": "http://www.rd.com/wp-content/uploads/sites/2/2016/02/06-train-cat-shake-hands.jpg"
            }
          ]
        }
      })
    return expect(result).to.be.rejected
  })
})
describe('/POST profile', () => {
  it("Creating an incomplete profile (empty died) should not work", function () {
    var result = chai.request(serverAddress)
      .post("/profile").send({
        "firstname": "Jane",
        "lastname": "Doe",
        "gender": "female",
        "birthDay": "1976-10-30",
        "deathDay": "",
        "profilePicture": "http://www.wikiality.com/file/2016/11/bears1.jpg",
        "born": {
          "name": "born",
          "description": "Jane is born",
          "location": {
            "city": "New York City",
            "province": "New York",
            "country": "U.S.A"
          },
          "media": [
            {
              "type": "image",
              "path": "http://www.rd.com/wp-content/uploads/sites/2/2016/02/06-train-cat-shake-hands.jpg"
            }
          ]
        },
        "died": {}
      })
    return expect(result).to.be.rejected
  })
})

describe("Login", () => {
  it("Trying to log in with an unregistered email address shouldn't work", function () {
    var result = chai.request(serverAddress).post("/auth").send({
      email: chance.email(),
      password: chance.password
    })
    return expect(result).to.be.rejected
  })
})
var account;
describe("Create account", () => {
  it("Creating an account with a correct profile id should work", function () {
    account = chai.request(serverAddress).post("/signup").send({
      email: chance.email(),
      password: "somePassword",
      claim: false,
      profileId: correctprofile.id
    })
    return account.then(function (response) {
      account = response.body;
      expect(response.status).to.equal(200)
    })
  })
})

describe("Creating account with same email", () => {
  it("Creating an account with the same email shouldn't work", function () {
    var invalidAccount = chai.request(serverAddress).post("/signup").send({
      email: account.email,
      password: "somePassword",
      claim: false,
      profileId: correctprofile.id
    })
    return expect(invalidAccount).to.be.rejected

  })
})

describe("Creating account", () => {
  it("Creating an account with new email and password but existing profile and no claim shouldn't work", function () {
    var result = chai.request(serverAddress).post("/signup").send({
      email: chance.email(),
      password: "somePassword",
      claim: false,
      profileId: correctprofile.id
    })
    return expect(result).to.be.rejected
  })
})

describe("Creating account", () => {
  it("Creating an account with existing profile but with claim = true should work", function () {
    var result = chai.request(serverAddress).post("/signup").send({
      email: chance.email(),
      password: "somePassword",
      claim: true,
      message: "Testing claims",
      profileId: correctprofile.id
    })
    return result.then(response => {
      expect(response.status).to.equal(200)
    })
  })
})

describe("Getting claims", () => {
  it("Getting claims from correct account should return at least one result", function () {
    var result = chai.request(serverAddress).get("/claims/" + account.id).set("requester", account.id);
    return result.then(response => {
      expect(response.body.length > 0);
    })
  })
})
var privateProfile
describe("Creating profile from correct account with private visibility", () => {
  it("Creating a profile with private visibility should work", function () {
    privateProfile = chai.request(serverAddress).post("/profile").set("requester", account.id).send({
      "firstname": "John",
      "lastname": "Doe",
      "gender": "female",
      "birthDay": "1976-10-30",
      "deathDay": "",
      "profilePicture": "http://www.wikiality.com/file/2016/11/bears1.jpg",
      "born": {
        "name": "born",
        "description": "Jane is born",
        "location": {
          "city": "New Yorkus",
          "province": "New York",
          "country": "U.S.A"
        },
        "media": [
          {
            "type": "image",
            "path": "http://www.rd.com/wp-content/uploads/sites/2/2016/02/06-train-cat-shake-hands.jpg"
          }
        ]
      },
      "visibility": {
        "visibility": "private"
      }
    });
    return privateProfile.then(function (response) {
      privateProfile = response.body;
      return chai.request(serverAddress).post("/ghost").send({
        ownerId: account.id,
        profileId: response.body.id
      }).then(response => {
        expect(response.status).to.equal(200)
      })
    })
  })
})
describe("Requesting private profile", () => {
  it("Getting a private profile without specifying a requester should return forbidden", function () {
    var result = chai.request(serverAddress).get("/profile/" + privateProfile.id);
    return expect(result).to.be.rejected
  })
})
describe("Requesting private profile", () => {
  it("Getting a private profile and specifying a correct requester should pass", function () {
    var result = chai.request(serverAddress).get("/profile/" + privateProfile.id).set("requester", account.id);
    return result.then(response => {
      return expect(response.status).to.equal(200)
    })
  })
})

describe("Requesing private profile", () => {
  it("Getting a private profile and specifying a wrong requester should not pass", function () {
    var result = chai.request(serverAddress).get("/profile/" + privateProfile.id).set("requester", 1000);
    return expect(result).to.be.rejected
  })
})

describe("GET /family", () => {
  it("Getting a private profile's family without a requester should not pass", function () {
    var result = chai.request(serverAddress).get("/family/" + privateProfile.id);
    return expect(result).to.be.rejected
  })
})
describe("GET /family", () => {
  it("Getting a private profile's family and specifying a correct requester should pass", function () {
    var result = chai.request(serverAddress).get("/family/" + privateProfile.id).set("requester", account.id);
    return result.then(response => {
      return expect(response.status).to.equal(200)
    })
  })
})

describe("GET /family", () => {
  it("Getting a private profile's family and specifying a wrong requester should not pass", function () {
    var randomAccount = chance.integer();
    while (randomAccount === account.id) {
      randomAccount = chance.integer();
    }
    var result = chai.request(serverAddress).get("/family/" + privateProfile.id).set("requester", randomAccount);
    return expect(result).to.be.rejected
  })
})

describe("POST /relationship", () => {
  it("POSTing a new relationship should not work if no requester is specified", function () {
    var result = chai.request(serverAddress).post("/relationship").send({});
    return expect(result).to.be.rejected
  })
})
describe("POST /relationship", () => {
  it("POSTing a new relationship should not work if request body is incomplete (1)", function () {
    var result = chai.request(serverAddress).post("/relationship").set("requester", account.id).send({});
    return expect(result).to.be.rejected
  })
})
var otherProfileId;
describe("POST /relationship", () => {
  it("POSTing a new relationship should not work if request body is incomplete (2)", function () {
    return chai.request(serverAddress).post("/profile").set("requester", account.id).send({
      "firstname": "John",
      "lastname": "Doe",
      "gender": "female",
      "birthDay": "1976-10-30",
      "deathDay": "",
      "profilePicture": "http://www.wikiality.com/file/2016/11/bears1.jpg",
      "born": {
        "name": "born",
        "description": "Jane is born",
        "location": {
          "city": "New Yorkus",
          "province": "New York",
          "country": "U.S.A"
        },
        "media": [
          {
            "type": "image",
            "path": "http://www.rd.com/wp-content/uploads/sites/2/2016/02/06-train-cat-shake-hands.jpg"
          }
        ]
      }
    }).then(response => {
      otherProfileId = response.body.id;
      return chai.request(serverAddress).post("/ghost").set("requester", account.id).send({
        profileId: otherProfileId,
        ownerId: account.id
      })
    }).then(response => {
      var result = chai.request(serverAddress).post("/relationship").set("requester", account.id).send({
        "profile1": privateProfile.id,
        "profile2": otherProfileId,
        "time": {
          "begin": "2010-10-10",
          "end": "2011-10-10"
        }
      });
      return expect(result).to.be.rejected
    })
  })
})

var relationshipId
describe("POST /relationship", () => {
  it("POSTing a new relationship should work if request body is complete and requester is specified", function () {
    return chai.request(serverAddress).post("/profile").set("requester", account.id).send({
      "firstname": "John",
      "lastname": "Doe",
      "gender": "female",
      "birthDay": "1976-10-30",
      "deathDay": "",
      "profilePicture": "http://www.wikiality.com/file/2016/11/bears1.jpg",
      "born": {
        "name": "born",
        "description": "Jane is born",
        "location": {
          "city": "New Yorkus",
          "province": "New York",
          "country": "U.S.A"
        },
        "media": [
          {
            "type": "image",
            "path": "http://www.rd.com/wp-content/uploads/sites/2/2016/02/06-train-cat-shake-hands.jpg"
          }
        ]
      }
    }).then(response => {
      otherProfileId = response.body.id;
      return chai.request(serverAddress).post("/ghost").set("requester", account.id).send({
        profileId: otherProfileId,
        ownerId: account.id
      })
    }).then(response => {
      console.log("Between " + privateProfile.id + " and " + otherProfileId)
      var result = chai.request(serverAddress).post("/relationship").set("requester", account.id).send({
        "profile1": privateProfile.id,
        "profile2": otherProfileId,
        "type": "partner",
        "time": {
          "begin": "2010-10-10"
        }
      });
      return result.then(response => {
        console.log(response.status)
        console.log(response.body)
        relationshipId = response.body.peopleentityid
        return expect(response.status).to.equal(200)
      });
    })
  })
})

describe("GET /family", () => {
  it("GETting family of a profile without specifying a correct requester that has a relationship with a private profile should return an empty relationship array and a people array of length 1", function () {
    var result = chai.request(serverAddress).get("/family/" + otherProfileId);
    return result.then(response => {
      return expect(response.body.relationships.length == 0 && response.body.people.length == 1)
    })
  })
})

describe("GET /family", () => {
  it("GETting family of a profile specifying a correct requester that has a relationship with a private profile should return a relationship array of length 1 and a people array of length 2", function () {
    var result = chai.request(serverAddress).get("/family/" + otherProfileId).set("requester", account.id);
    return result.then(response => {
      return expect(response.body.relationships.length == 1 && response.body.people.length == 2)
    })
  })
})
var childId;
describe("POST /parent", () => {
  it("POSTing a new parent should not work if request body is incomplete", function () {
    return chai.request(serverAddress).post("/profile").set("requester", account.id).send({
      "firstname": "John Jr",
      "lastname": "Doe",
      "gender": "female",
      "birthDay": "1989-10-30",
      "deathDay": "",
      "profilePicture": "http://www.wikiality.com/file/2016/11/bears1.jpg",
      "born": {
        "name": "born",
        "description": "John jr is born",
        "location": {
          "city": "New Yorkus",
          "province": "New York",
          "country": "U.S.A"
        },
        "media": [
          {
            "type": "image",
            "path": "http://www.rd.com/wp-content/uploads/sites/2/2016/02/06-train-cat-shake-hands.jpg"
          }
        ]
      }
    }).then(response => {
      childId = response.body.id;
      return chai.request(serverAddress).post("/ghost").set("requester", account.id).send({
        profileId: childId,
        ownerId: account.id
      })
    }).then(response => {
      var result = chai.request(serverAddress).post("/parents").set("requester", account.id).send({
        "parentType": "guardian",
        "parent": relationshipId,
        "time": {
          begin: "1990-07-07"
        }
      });
      return expect(result).to.be.rejected
    })
  })
})
describe("POST /parent", () => {
  it("POSTing a new parent should not work if requester is not specified", function () {
    return chai.request(serverAddress).post("/profile").set("requester", account.id).send({
      "firstname": "John Jr",
      "lastname": "Doe",
      "gender": "female",
      "birthDay": "1989-10-30",
      "deathDay": "",
      "profilePicture": "http://www.wikiality.com/file/2016/11/bears1.jpg",
      "born": {
        "name": "born",
        "description": "John jr is born",
        "location": {
          "city": "New Yorkus",
          "province": "New York",
          "country": "U.S.A"
        },
        "media": [
          {
            "type": "image",
            "path": "http://www.rd.com/wp-content/uploads/sites/2/2016/02/06-train-cat-shake-hands.jpg"
          }
        ]
      }
    }).then(response => {
      childId = response.body.id;
      return chai.request(serverAddress).post("/ghost").set("requester", account.id).send({
        profileId: childId,
        ownerId: account.id
      })
    }).then(response => {
      var result = chai.request(serverAddress).post("/parents").send({
        "parentType": "guardian",
        "parent": relationshipId,
        "child": childId,
        "time": {
          begin: "1990-07-07"
        }
      });
      return expect(result).to.be.rejected
    })
  })
})
describe("POST /parent", () => {
  it("POSTing a new parent should not work if request body is incomplete", function () {
    return chai.request(serverAddress).post("/profile").set("requester", account.id).send({
      "firstname": "John Jr",
      "lastname": "Doe",
      "gender": "female",
      "birthDay": "1989-10-30",
      "deathDay": "",
      "profilePicture": "http://www.wikiality.com/file/2016/11/bears1.jpg",
      "born": {
        "name": "born",
        "description": "John jr is born",
        "location": {
          "city": "New Yorkus",
          "province": "New York",
          "country": "U.S.A"
        },
        "media": [
          {
            "type": "image",
            "path": "http://www.rd.com/wp-content/uploads/sites/2/2016/02/06-train-cat-shake-hands.jpg"
          }
        ]
      }
    }).then(response => {
      childId = response.body.id;
      return chai.request(serverAddress).post("/ghost").set("requester", account.id).send({
        profileId: childId,
        ownerId: account.id
      })
    }).then(response => {
      var result = chai.request(serverAddress).post("/parents").set("requester", account.id).send({
        "parentType": "guardian",
        "parent": relationshipId,
        "child": childId,
        "time": {
          begin: "1990-07-07"
        }
      });
      return result.then(response => {
        console.log(response.body);
        expect(response.status).to.equal(200)
      })
    })
  })
})