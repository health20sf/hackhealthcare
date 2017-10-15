/*

Botkit Studio Skill module to enhance the "scratch" script

*/

var age = "";
var sex = "";
var fam = "";
var hbp = "";
var phy = "";
var hgt = "";
var wgt = "";
var zip = "";
var score = 0;
var dob = "";
var first = "";
var last  = "";
var phone = "";
var docs = [];
var insuranceType = "";
var mappt = "";
var appts = [];
var results = [];
var type = "";


const requestPromise = require('request-promise')

function doctorSearch(location, radius, limit, type, callback) {
  const api_key = process.env.BETTERDOCTOR_COM_API_KEY
  const resource_url = 'https://api.betterdoctor.com/2016-03-01/doctors?location='+location+','+radius+'&skip=2&limit='+limit+'&user_key=' + api_key;

  var bdOpts = {
    uri: resource_url,
    specialty_uid: type,
    method: 'GET',
    json: true,
    resolveWithFullResponse: true
  }
  let retInfo = ""
  requestPromise(bdOpts)
  .then(data => {
    // console.log(data.body)
    console.log('doctorSearch results')
    console.log('')
    console.log('data.body.meta')
    console.log('- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ')
    console.log(data.body.meta)
    console.log('')
    console.log('data.body.data[]')
    console.log('- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ')
    let bounce = 0
    for (let dataNode of data.body.data) {
      let iretInfo = ''
      bounce++
      if (bounce > 3) {
        break
      }
      const profile = dataNode.profile
      const specialties = dataNode.specialties
      const practices = dataNode.practices  // array of location objects
      const insurances = dataNode.insurances  // array of {plan: provider}

      console.log(profile.first_name + ' ' + profile.middle_name + ' ' + profile.last_name)
      iretInfo += profile.first_name + ' ' + profile.middle_name + ' ' + profile.last_name + '\n'
      // profile has property image_url

      console.log('   -----')
      iretInfo += '   -----\n'
      console.log('   -----')
      iretInfo += '   -----\n'
      let dist = ''
      for (let practice of practices) {
        console.log('   ' + practice.name + ' (' + practice.distance + ' miles)')
        iretInfo += '   ' + practice.name + ' (' + Math.round(practice.distance) + ' miles)\n'
        dist = Math.round(practice.distance)
      }
      // {
      //     "name":"yes",
      //     "text": "Yes",
      //     "value": "yes",
      //     "style": "default",
      //     "type": "button",
      // }
      let dstr = profile.first_name + ' ' + profile.last_name + ' (' + dist + ' miles)'
      let clinic = 'Dr. ' + profile.first_name + ' ' + profile.last_name
      docs.push({
        name: "doctorpractice",
        text: dstr,
        value: clinic,
        style: "default",
        type: "button"
      })
      console.log('   -----')
      iretInfo += '   -----\n'
      let count = 0
      for (let insurance of insurances) {
        count++
        if (count > 3) {
          console.log('   ...')
          iretInfo += '   ...\n'
          break
        }

        // console.log('   insurance_plan:')
        // for (let propName in insurance.insurance_plan) {
        //   console.log('   ' + propName + ': ' + insurance.insurance_plan[propName])
        // }
        // console.log('   insurance_provider:')
        // for (let propName in insurance.insurance_provider) {
        //   console.log('   ' + propName + ': ' + insurance.insurance_provider[propName])
        // }
        console.log('   ' + insurance.insurance_plan.name + ' ('+ insurance.insurance_provider.name +')')
        iretInfo += '   ' + insurance.insurance_plan.name + ' ('+ insurance.insurance_provider.name +')\n'

      }
      console.log('')
      if (iretInfo)
        retInfo += iretInfo
    }

    callback(retInfo);
  })
  .catch(error => {
    console.log('error', error)
    return
  })
}
// The keys below are height in feet and inches with the quotes removed.
// For example:  4'10" --> 410
//
const heightWeightLUT = {
  '410': [119, 142, 143, 190, 191],
  '411': [124, 147, 148, 197, 198],
  '50': [128, 152, 153, 203, 204],
  '51': [132, 157, 158, 210, 211],
  '52': [136, 163, 164, 217, 218],
  '53': [141, 168, 169, 224, 225],
  '54': [145, 173, 174, 231, 232],
  '55': [150, 179, 180, 239, 240],
  '56': [155, 185, 186, 246, 247],
  '57': [159, 190, 191, 254, 255],
  '58': [164, 196, 197, 261, 262],
  '59': [169, 202, 203, 269, 270],
  '510': [174, 208, 209, 277, 278],
  '511': [179, 214, 215, 285, 286],
  '60': [184, 220, 221, 293, 294],
  '61': [189, 226, 227, 301, 302],
  '62': [194, 232, 233, 310, 311],
  '63': [200, 239, 240, 318, 319],
  '64': [205, 245, 246, 327, 328]
}

function getHeightWeightScore(height, weight) {
  console.log('getHeightWeightScore:')
  if (!height || !weight) {
    console.log('  returning -1: height or weight undefined/null/etc.')
    return -1
  }

  // TODO: put this in a single one liner regex w/ character classes
  let fixQuotesHeight = height.replace('’', '')
  fixQuotesHeight = fixQuotesHeight.replace('\'', '')
  fixQuotesHeight = fixQuotesHeight.replace('”', '')
  fixQuotesHeight = fixQuotesHeight.replace('"', '')
  fixQuotesHeight = fixQuotesHeight.replace(' ', '')

  if (!(fixQuotesHeight in heightWeightLUT)) {
    console.log('  returning -1: fixQuotesHeight('+fixQuotesHeight+') not in heightWeightLUT')
    return -1
  }

  console.log('  fixQuotesHeight:' + fixQuotesHeight)
  console.log('  weight:'+weight)
  const boundsArr = heightWeightLUT[fixQuotesHeight]
  if (weight >=  boundsArr[0] && weight <= boundsArr[1]) {
    return 1
  } else if (weight >= boundsArr[2] && weight <= boundsArr[3]) {
    return 2
  } else if (weight >= boundsArr[4]) {
    return 3
  }
  return 0
}

let events = require('events')
let https = require('https')
let querystring = require('querystring')

const key = process.env.ATHENAHEALTH_API_KEY
const secret = process.env.ATHENAHEALTH_SECRET
const version = 'preview1'
const practiceid = 195900

const auth_prefixes = {
    v1: '/oauth',
    preview1: '/oauthpreview',
    openpreview1: '/oauthopenpreview',
}

const api_hostname = 'api.athenahealth.com'

// This is a useful function to have
function path_join() {
    // trim slashes from arguments, prefix a slash to the beginning of each, re-join (ignores empty parameters)
    var args = Array.prototype.slice.call(arguments, 0)
    var nonempty = args.filter(function(arg, idx, arr) {
        return typeof(arg) != 'undefined'
    })
    var trimmed = nonempty.map(function(arg, idx, arr) {
        return '/' + String(arg).replace(new RegExp('^/+|/+$'), '')
    })
    return trimmed.join('')
}
// Appointmenttypeid 2, 82 and 683 work so far.
// dummydata
var appointmentData = {
    practiceid: practiceid,
    departmentid: 1,
    patientid: undefined,
    providerid: 71,
    appointmenttypeid: 2,
    appointmentid: undefined
}
// var appointmentData = {
//     practiceid: practiceid,
//     departmentid: 1,
//     patientid: 30837,
//     providerid: 71,
//     appointmenttypeid: 2,
//     appointmentid:883988
// }

// Since we want these functions to run in a set order, we need a way to signal for the next one.
var signal = new events.EventEmitter

// We need to save the token in an outer scope, because of callbacks.
var token = process.env.ATHENAHEALTH_TOKEN
//var token = undefined

function authentication() {
  // TODO: token needs to be updated once per hour or you'll get a 401 error
  // repsonse. Getting it for every API call will result in getting locked out.
  //
  if (token) {
    console.log('Using athenahealth token from env file: \'' + token + '\'')
    console.log('   If you see "<h1>Developer Inactive</h1>" then refresh the token.')
        signal.emit('next')
  } else {
    var req = https.request({
      // Set up the request, making sure the content-type header is set. Let the https library do
      // the auth header (including base64 encoding) for us.
      hostname: api_hostname,
      method: 'POST',
      path: path_join(auth_prefixes[version], '/token'),
      auth: key + ':' + secret,
      headers: {'content-type': 'application/x-www-form-urlencoded'},
    }, function(response) {
      response.setEncoding('utf8')
      var content = ''
      response.on('data', function(chunk) {
        content += chunk
      })
      response.on('end', function() {
        var authorization = JSON.parse(content)
        // Save the token!
        token = authorization.access_token
        console.log(token)
        signal.emit('next')
      })
    })

    req.on('error', function(e) {
      console.log(e.message)
    })

    // The one parameter required for OAuth
    req.write(querystring.stringify({grant_type: 'client_credentials'}))
    req.end()
  }
}

// Patients we've created:
//  30836, 30837
function createPatient() {
// dummydata

    let parameters = {
        departmentid:appointmentData.departmentid,
        dob: dob,
        firstname: first,
        homephone: phone,
        lastname: last
    }
    // let parameters = {
    //     departmentid:appointmentData.departmentid,
    //     dob:'1/1/1970',
    //     firstname:'Jason',
    //     homephone:'408-746-8488',
    //     lastname:'Foo'
    // }
    const content = querystring.stringify(parameters)

    const patientsPath = path_join(version, practiceid, '/patients')
    console.log('patientsPath: \'' + patientsPath + '\'')

    var req = https.request({
        hostname: api_hostname,
        method: 'POST',
        path: patientsPath,
        headers: {
            'authorization': 'Bearer ' + token,
            'content-type': 'application/x-www-form-urlencoded',
            'content-length': content.length, // apparently we have to set this ourselves when using
                                                              // application/x-www-form-urlencoded
        },
    }, function(response) {
        response.setEncoding('utf8')
        var content = ''
        response.on('data', function(chunk) {
            content += chunk
        })
        response.on('end', function() {
            console.log('Patient added:')
            const patientData = JSON.parse(content)
            console.log(patientData)
            appointmentData.patientid = patientData[0].patientid

            signal.emit('next')
        })
    })
    req.on('error', function(e) {
        console.log(e.message)
    })

    req.write(content)
    req.end()
}

function findAppointmentSlots() {
    // Create and encode parameters
    const parameters = {
        appointmenttypeid:appointmentData.appointmenttypeid,
        departmentid:appointmentData.departmentid,
        providerid:appointmentData.providerid,
        ignoreschedulablepermission:false,
        limit:4,
        offset:0,
        startdate:'11/14/2017',
        enddate:'11/21/2017'
    }
    const query = '?' + querystring.stringify(parameters)

  const aptsPath = path_join(version, practiceid, 'appointments', 'open') + query
  console.log('aptsPath: \'' + aptsPath + '\'')

    var req = https.request({
        hostname: api_hostname,
        method: 'GET',
        path: aptsPath,
        // We set the auth header ourselves this time, because we have a token now.
        headers: {'authorization': 'Bearer ' + token},
    }, function(response) {
        response.setEncoding('utf8')
        var content = ''
        response.on('data', function(chunk) {
            content += chunk
        })
        response.on('end', function() {
            console.log('Appointments:')
            const apptData = JSON.parse(content)

            console.log(apptData.totalcount + ' appointments found.')

            for (let appointment of apptData.appointments) {
                console.log('   appointmentid:' + appointment.appointmentid)
                console.log('   ' + appointment.starttime + ' ' + appointment.date)
                console.log('   ' + appointment.duration + ' minutes')
                console.log()
                // appts += '   appointmentid:' + appointment.appointmentid
                // appts += '   ' + appointment.starttime + ' ' + appointment.date.replace(/\//g, "-")
                // appts += '   ' + appointment.duration + ' minutes\n'
                // {
                //     "name":"yes",
                //     "text": "Yes",
                //     "value": "yes",
                //     "style": "primary",
                //     "type": "button",
                // }
                let str = appointment.starttime + ' ' + appointment.date.replace(/\//g, "-")
                appts.push({
                    name: 'appointmentvalues',
                    text: str,
                    value: appointment.appointmentid,
                    style: "primary",
                    type: "button"
                })
            }
            signal.emit('next')
        })
    })
    req.on('error', function(e) {
        console.log(e.message)
    })

    req.end()
}

function scheduleAppointment() {
    let patientid = appointmentData.patientid ?
        appointmentData.patientid : 30837

    let appointmentid = appointmentData.appointmentid
    if (!appointmentid) {
        console.log('scheduleAppointment: setting appointmentid to slot 0')
        const apptSlot0 = appointmentData.appointmentSlots[0]
        appointmentid = apptSlot0.appointmentid
    }

    // Create and encode parameters
    let parameters = {
        appointmenttypeid:appointmentData.appointmenttypeid,
        patientid:patientid,
        ignoreschedulablepermission:false
    }
    const query = '?' + querystring.stringify(parameters)

    const apptPath = path_join(
        version, practiceid, 'appointments', appointmentid) + query
    console.log('apptPath: \'' + apptPath + '\'')

    var req = https.request({
        hostname: api_hostname,
        method: 'PUT',
        path: apptPath,
        headers: {
            'authorization': 'Bearer ' + token
        },
    }, function(response) {
        response.setEncoding('utf8')
        var content = ''
        response.on('data', function(chunk) {
            content += chunk
        })
        response.on('end', function() {
            console.log('Patient appointment scheduled:')
            const apptData = JSON.parse(content)
            console.log(apptData)
            appointmentData.appointmentScheduled = apptData
            signal.emit('nextSchedule')
        })
    })
    req.on('error', function(e) {
        console.log(e.message)
    })

    // req.write(content)
    req.end()
}

function recordPatientIssue() {
    let parameters = {
        departmentid:appointmentData.departmentid,
        snomedcode:43396009
    }
    const content = querystring.stringify(parameters)

    let patientid = appointmentData.patientid ?
        appointmentData.patientid : 30837

    const problemsPath = path_join(
        version, practiceid, 'chart', patientid, 'problems')

    console.log('problemsPath: \'' + problemsPath + '\'')

    var req = https.request({
        hostname: api_hostname,
        method: 'POST',
        path: problemsPath,
        headers: {
            'authorization': 'Bearer ' + token,
            'content-type': 'application/x-www-form-urlencoded',
            'content-length': content.length, // apparently we have to set this ourselves when using
                                                              // application/x-www-form-urlencoded
        },
    }, function(response) {
        response.setEncoding('utf8')
        var content = ''
        response.on('data', function(chunk) {
            content += chunk
        })
        response.on('end', function() {
            console.log('Patient problem added:')
            const patientProblemData = JSON.parse(content)
            console.log(patientProblemData)
            signal.emit('next')
        })
    })
    req.on('error', function(e) {
        console.log(e.message)
    })

    req.write(content)
    req.end()
}

module.exports = function(controller) {
    controller.hears('another_keyword','direct_message,direct_mention',function(bot,message) {
      var reply_with_attachments = {
        'username': 'My bot' ,
        'text': 'This is a pre-text',
        'attachments': [
          {
            'fallback': 'To be useful, I need you to invite me in a channel.',
            'title': 'How can I help you?',
            'text': 'To be useful, I need you to invite me in a channel ',
            'color': '#7CD197',
          }
        ],
        'icon_url': 'http://lorempixel.com/48/48'
        }

      bot.reply(message, reply_with_attachments);
    });


    controller.hears(['^risk$'], 'direct_message, direct_mention, ambient, mention', function(bot, message) {
      bot.startConversation(message, function(err, convo){
        //END THE CONVERSATION
        convo.addMessage({
            text: 'Ok...let\'s try another time'
        }, 'end_convo');

        //CONFIRM START
        convo.addQuestion({
            text: 'Welcome to the prediabetes risk assessment.',
            attachments:[
                {
                    title: 'Do you want to proceed?',
                    callback_id: '1',
                    attachment_type: 'default',
                    actions: [
                        {
                            "name":"yes",
                            "text": "Yes",
                            "value": "yes",
                            "style": "primary",
                            "type": "button",
                        },
                        {
                            "name":"no",
                            "text": "No",
                            "value": "no",
                            "style": "danger",
                            "type": "button",
                        }
                    ]
                }
            ]
        },[
            {
                pattern: "yes",
                callback: function(response, convo) {
                    convo.gotoThread('get_age');
                },
            },
            {
                pattern: "no",
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                },
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ], {}, 'default');

        //GET THE AGE FROM THE USER
        convo.addQuestion({
            attachments:[
                {
                    title: 'Pick your age range',
                    callback_id: '9',
                    attachment_type: 'default',
                    actions: [
                        {
                            "name":"40",
                            "text": "10 - 39",
                            "value": "38",
                            "style": "default",
                            "type": "button",
                        },
                        {
                            "name":"50",
                            "text": "40 - 49",
                            "value": "45",
                            "style": "default",
                            "type": "button",
                        },
                        {
                            "name":"60",
                            "text": "50 - 59",
                            "value": "55",
                            "style": "default",
                            "type": "button",
                        },
                        {
                            "name":"70",
                            "text": "60+",
                            "value": "70",
                            "style": "default",
                            "type": "button",
                        }
                    ]
                }
            ]
        },[
            {
                pattern: /[0-9]+/g,
                callback: function(response, convo) {
                    age = response.text
                    console.log('AGE;', age)
                    if (age >= 40 && age <= 49) {
                        score += 1
                    } else if (age >= 50 && age <= 59) {
                        score += 2
                    } else if (age >= 60) {
                        score += 3
                    }
                    convo.gotoThread('get_sex');
                }
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ], {}, 'get_age');

        //GET THE SEX FROM THE USER
        convo.addQuestion({
            attachments:[
                {
                    title: 'Male or Female?',
                    callback_id: '2',
                    attachment_type: 'default',
                    actions: [
                        {
                            "name":"male",
                            "text": "Male",
                            "value": "male",
                            "style": "default",
                            "type": "button",
                        },
                        {
                            "name":"female",
                            "text": "Female",
                            "value": "female",
                            "style": "default",
                            "type": "button",
                        }
                    ]
                }
            ]
        },[
            {
                pattern: "male",
                callback: function(response, convo) {
                    sex = response.text.toLowerCase()
                    console.log('male', sex)
                    score += 1
                    if (sex === 'male')
                        convo.gotoThread('get_family');
                    else
                        convo.gotoThread('get_gestational');
                }
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ], {}, 'get_sex');

        //GET THE GESTATIONAL INFO FROM THE FEMALE USER
        convo.addQuestion({
            attachments:[
                {
                    title: 'Have you ever been diagnosed with gestational diabetes?',
                    callback_id: '3',
                    attachment_type: 'default',
                    actions: [
                        {
                            "name":"yes",
                            "text": "Yes",
                            "value": "yes",
                            "style": "default",
                            "type": "button",
                        },
                        {
                            "name":"no",
                            "text": "No",
                            "value": "no",
                            "style": "default",
                            "type": "button",
                        }
                    ]
                }
            ]
        },[
            {
                pattern: "yes",
                callback: function(response, convo) {
                    fam = response.text
                    score += 1
                    convo.gotoThread('get_family');
                },
            },
            {
                pattern: "no",
                callback: function(response, convo) {
                    convo.gotoThread('get_family');
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ], {}, 'get_gestational');

        //GET THE FAMIILY HISTORY
        convo.addQuestion({
            attachments:[
                {
                    title: 'Do you have a mother, father, sister, or brother with diabetes?',
                    callback_id: '4',
                    attachment_type: 'default',
                    actions: [
                        {
                            "name":"yes",
                            "text": "Yes",
                            "value": "yes",
                            "style": "default",
                            "type": "button",
                        },
                        {
                            "name":"no",
                            "text": "No",
                            "value": "no",
                            "style": "default",
                            "type": "button",
                        }
                    ]
                }
            ]
        },[
            {
                pattern: "yes",
                callback: function(response, convo) {
                    fam = response.text
                    score += 1
                    convo.gotoThread('get_bp');
                },
            },
            {
                pattern: "no",
                callback: function(response, convo) {
                    convo.gotoThread('get_bp');
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ], {}, 'get_family');

        //GET THE BLOOD PRESSURE
        convo.addQuestion({
            attachments:[
                {
                    title: 'Have you ever been diagnosed with high blood pressure?',
                    callback_id: '5',
                    attachment_type: 'default',
                    actions: [
                        {
                            "name":"yes",
                            "text": "Yes",
                            "value": "yes",
                            "style": "default",
                            "type": "button",
                        },
                        {
                            "name":"no",
                            "text": "No",
                            "value": "no",
                            "style": "default",
                            "type": "button",
                        }
                    ]
                }
            ]
        },[
            {
                pattern: "yes",
                callback: function(response, convo) {
                    hbp = response.text
                    score += 1
                    convo.gotoThread('get_physical_activity');
                },
            },
            {
                pattern: "no",
                callback: function(response, convo) {
                    hbp = response.text
                    convo.gotoThread('get_physical_activity');
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'get_bp');

        //GET THE PHYSICAL ACTIVITY
        convo.addQuestion({
            attachments:[
                {
                    title: 'Are you physically active?',
                    callback_id: '6',
                    attachment_type: 'default',
                    actions: [
                        {
                            "name":"yes",
                            "text": "Yes",
                            "value": "yes",
                            "style": "default",
                            "type": "button",
                        },
                        {
                            "name":"no",
                            "text": "No",
                            "value": "no",
                            "style": "default",
                            "type": "button",
                        }
                    ]
                }
            ]
        },[
            {
                pattern: "yes",
                callback: function(response, convo) {
                    phy = response.text
                    convo.gotoThread('get_height');
                },
            },
            {
                pattern: "no",
                callback: function(response, convo) {
                    phy = response.text
                    score += 1
                    convo.gotoThread('get_height');
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'get_physical_activity');

        //GET THE HEIGHT
        convo.addQuestion('What is your height? (e.g.: 5 0  or  5\'0")',[
            {
                pattern: '.*',
                callback: function(response, convo) {
                    hgt = response.text
                    convo.gotoThread('get_weight');
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'get_height');

        //GET THE WEIGHT
        convo.addQuestion('What is your weight in pounds? (e.g.: 185)',[
            {
                pattern: /[0-9]+/g,
                callback: function(response, convo) {
                    wgt = response.text
                    score += getHeightWeightScore(hgt, wgt)
                    if (score < 5) {
                        convo.gotoThread('notdiabetic');
                    }
                    else {
                        convo.gotoThread('diabetic')
                    }
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'get_weight');

        //GET DIABETES HELP
        convo.addQuestion({
            text: 'From your answers, it appears you are at increased risk of having Type 2 Diabetes.',
            attachments:[
                {
                    title: 'Would you like us to find a clinic for a HBA1C test?',
                    callback_id: '7',
                    attachment_type: 'default',
                    actions: [
                        {
                            "name":"yes",
                            "text": "Yes",
                            "value": "yes",
                            "style": "default",
                            "type": "button",
                        },
                        {
                            "name":"no",
                            "text": "No",
                            "value": "no",
                            "style": "default",
                            "type": "button",
                        }
                    ]
                }
            ]
        },[
            {
                pattern: "yes",
                callback: function(response, convo) {
                    convo.gotoThread('get_zipcode');
                },
            },
            {
                pattern: "no",
                callback: function(response, convo) {
                    convo.gotoThread('finish');
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'diabetic');

        //GET USER ZIPCODE
        convo.addQuestion('What is your zipcode? (e.g. 94502)',[
            {
                pattern: /[0-9]+/g,
                callback: function(response, convo) {
                    zip = response.text
                    var zipcodes = require('zipcodes');
                    var location = zipcodes.lookup(zip)
                    var locStr = location.latitude.toString() + ',' + location.longitude.toString()
                    doctorSearch(locStr, 25, 10, type, function(response){
                        // convo.say(response); // add another reply
                        convo.setVar('results',response);
                        convo.gotoThread('get_insur')
                    });
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'get_zipcode');

        convo.addQuestion({
            "response_type": "in_channel",
            "attachments": [
                {
                    "text": "Who is your insurance provider?",
                    "color": "#3AA3E3",
                    "attachment_type": "default",
                    "callback_id": "insurance_provider",
                    "actions": [
                    {
                      "name": "games_list",
                      "text": "Pick one",
                      "type": "select",
                      "options": [
                          {
                              "text": "I don't know",
                              "value":  '*',
                          },
                          {
                              "text": "Aetna",
                              "value":  'aetna',
                          },
                          {
                              "text": "Anthem",
                              "value":  'anthem',
                          },
                          {
                              "text": "Blue Shield of California",
                              "value":  'blueshieldofcalifornia'
                          },
                          {
                              "text": "Cigna",
                              "value":  'cigna',
                          },
                          {
                              "text": "Coventry Health Care",
                              "value":  'coventryhealthcare',
                          },
                          {
                              "text": "Health Net",
                              "value":  'healthnet',
                          },
                          {
                              "text": "Humana",
                              "value":  'humana',
                          },
                          {
                              "text": "Kaiser Permanente",
                              "value":  'kaiserpermanente',
                          },
                          {
                              "text": "MetLife",
                              "value":  'metlife',
                          },
                          {
                              "text": "PacificSource Health Plans",
                              "value":  'pacificsourcehealthplans',
                          },
                          {
                              "text": "Providence Health System",
                              "value":  'providencehealthsystem',
                          },
                          {
                              "text": "United Healthcare",
                              "value":  'unitedhealthcare'
                          }
                      ]
                    }
                  ]
                }
            ]
        },[
            {
                pattern: /[A-Za-z\. ]+/g,
                callback: function(response, convo) {
                    insuranceType = response.text
                    convo.gotoThread('get_docs');
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'get_insur');

        convo.addQuestion({
            attachments:[
                {
                    title: 'Here are the nearest doctors available',
                    callback_id: '17',
                    attachment_type: 'default',
                    actions: docs
                }
            ]
        },[
            {
                pattern: /[A-Za-z\. ]+/g,
                callback: function(response, convo) {
                    console.log('RESPO', response.text)
                    convo.gotoThread('get_first_name');
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'get_docs');

        //GET USER FIRST NAME
        convo.addQuestion('What is your first name?',[
            {
                pattern: /[a-zA-Z]+/g,
                callback: function(response, convo) {
                    first = response.text
                    convo.gotoThread('get_last_name')
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'get_first_name');

        //GET USER LAST NAME
        convo.addQuestion('What is your last name?',[
            {
                pattern: /[a-zA-Z]+/g,
                callback: function(response, convo) {
                    last = response.text
                    convo.gotoThread('get_dob')
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'get_last_name');

        //GET USER DOB
        convo.addQuestion('What is your date of birth? (e.g 10/15/1970)',[
            {
                pattern: /[0-9\/]+/g,
                callback: function(response, convo) {
                    dob = response.text
                    convo.gotoThread('get_phone_number')
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'get_dob');

        //GET USER PHONE NUMBER
        convo.addQuestion('What is your phone number?\n`Note: Please be patient while we find your appointment.`',[
            {
                pattern: /[0-9\-\(\)]+/g,
                callback: function(response, convo) {
                    phone = response.text
                    console.log('Ok looking for clinic near ' + zip + ' for ' + first + ' ' + last + ', phone: ' + phone)
                    let calls = [authentication, createPatient, recordPatientIssue, findAppointmentSlots]
                    signal.on('next', function() {
                        let nextCall = calls.shift()
                        if (nextCall) {
                            console.log('processing ...')
                            // convo.say('processing...')
                            // convo.next()
                            nextCall()
                        } else {
                            convo.gotoThread('set_appt')
                        }
                    })
                    signal.emit('next')
                    // convo.gotoThread('finish')
                },
            },
            {
                pattern: bot.utterances.quit,
                callback: function(response, convo) {
                    convo.gotoThread('end_convo');
                }
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'get_phone_number');
      
        convo.addQuestion({
            text: 'Here are your appointment options\n`Note: Please be patient while we book your appointment.`',
            attachments:[
                {
                    title: 'Please pick one',
                    callback_id: '8',
                    attachment_type: 'default',
                    actions: appts
                }
            ]
        },[
            {
                pattern: /[0-9]+/g,
                callback: function(response, convo) {
                    console.log('Response from brokern', response.text)
                    mappt = response.text
                    appointmentData.appointmentid = mappt
                    let calls2 = [scheduleAppointment]
                    signal.on('nextSchedule', function() {
                        let nextCall = calls2.shift()
                        if (nextCall) {
                            console.log('WORKING PLEASE')
                            // convo.say('processing...')
                            // convo.next()
                            nextCall()
                        } else {
                            console.log('DOUBLE TROUBLE')
                            convo.gotoThread('appt_finish')    
                        }
                    })
                    signal.emit('nextSchedule')
                },
            },
            {
                default: true,
                callback: function(response, convo) {
                    convo.repeat();
                    convo.next();
                }
            }
        ],{}, 'set_appt')

        convo.addMessage({text: 'Ok looking for clinic near {{vars.zip}} for {{vars.first}} {{vars.last}} at phone: {{vars.phone}}'}, 'repeat_back')
        convo.addMessage({text: 'Congratulations! From the answers you provided, it does not appear that you are at increased risk for having Type 2 Diabetes.'}, 'notdiabetic')
        convo.addMessage({text: 'Help us spread the word about Type 2 Diabetes! Share the chatbot with your friends and family 🎁!'},'finish');
        convo.addMessage({text: 'Ok, great we have you scheduled for that appointment!'},'appt_finish');
      })
    })
}