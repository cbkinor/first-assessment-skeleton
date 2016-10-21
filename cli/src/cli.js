import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let lastCommand
const PORT = '8080'
const HOST = 'localhost'
// var colors = require('colors/safe')

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
.mode('connect <username> [host] [port]')
.delimiter(cli.chalk['green']('connected>'))
.init(function (args, callback) {
  username = args.username
  let host = (args.host) ? args.host	: HOST
  let port = (args.port) ? args.port : PORT
  server = connect({ host, port }, () => {
    server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
    callback()
  })

  server.on('data', (buffer) => {
    let message = Message.fromJSON(buffer)
    let m = message.toString()
    if (message.command === 'connect' || message.command === 'disconnect') {
      cli.log(cli.chalk['green'](m))
    } else if (message.command === 'echo') {
      cli.log(cli.chalk['yellow'](m))
    } else if (message.command === 'broadcast') {
      cli.log(cli.chalk['red'](m))
    } else if (message.command.charAt(0) === '@') {
      cli.log(cli.chalk['magenta'](m))
    } else if (message.command === 'users') {
      cli.log(cli.chalk['blue'](m))
    }
  })

  server.on('end', () => {
    cli.exec('exit')
  })
})
  .action(function (input, callback) {
    let [ command, ...rest ] = words(input, /[^\s]+/g)
    let contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      lastCommand = command
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'broadcast') {
      lastCommand = command
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'users') {
      lastCommand = command
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command.charAt(0) === '@') {
      lastCommand = command
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (lastCommand !== undefined) {
      contents = command + ' ' + contents
      command = lastCommand
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else {
      this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })
