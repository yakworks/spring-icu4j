package testify

import grails.config.Config
import grails.core.GrailsApplication
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.message.Msg
import yakworks.message.MsgContext
import yakworks.message.MsgKey
import yakworks.i18n.icu.ICUMessageSource

// Use as a simple to test when trying to see why application context has problem on init
@Integration
class SanityCheckSpec extends Specification {

    GrailsApplication grailsApplication
    ICUMessageSource messageSource

    void 'check config'() {
        when:
        Config config = grailsApplication.config

        then:
        "FOO" == config.foo
        "BAR" == config.bar
        'FOO/BAR' == config.fooBar
        'FOO/$bar' == config.fooBarDollar
    }

    void 'maps for named arguments'() {
        when:
        MsgKey msgKey = Msg.key('named.arguments', [name: 'foo'])

        String msg = messageSource.get(msgKey)

        then:
        "Attachment foo saved" == msg
    }

    void 'without arguments'() {
        when:
        MsgKey msgKey = Msg.key('named.arguments')

        String msg = messageSource.get(msgKey)

        then:
        "Attachment {name} saved" == msg
    }

    void "should pick up messages.yaml"() {
        expect:
        //old way
        expected == messageSource.getMessage(key, [] as Object[], locale)
        //new way
        expected == messageSource.get(key, MsgContext.of(locale))

        where:
        key             | locale         | expected
        'go'            | Locale.ENGLISH | "Go Go Go" //exists in both
        'go'            | Locale.FRENCH  | "aller aller aller"
        'testing.emoji' | Locale.ENGLISH | "I am 🔥" //exists in yml
        'testing.emoji' | Locale.FRENCH  | "je suis 🔥"
        'testing.go'    | Locale.ENGLISH | "got it" //exists only in messages.yml
        'testing.go'    | Locale.FRENCH  | "got it" //exists only in messages.yml, not fr
    }

    void 'does it pick up ValidationMessages files'() {
        expect:
        'must be less than or equal to 1' == messageSource.get('jakarta.validation.constraints.Max.message', [value:1])
        'Got it' == messageSource.get('some.validation.message')
    }

    void 'with default'() {
        when:
        MsgKey msgKey = Msg.key('nonexistent').fallbackMessage("no such animal")

        String msg = messageSource.get(msgKey)

        then:
        'no such animal' == msg
    }

    void 'when default has named args'() {
        when:
        MsgKey msgKey = Msg.key('nonexistent', [name: 'taco']).fallbackMessage("have a {name} 🌮")

        String msg = messageSource.get(msgKey)

        then:
        "have a taco 🌮" == msg
    }

    void 'when args have an emoji'() {
        when:
        MsgKey msgKey = Msg.key('nonexistent', [name: 'taco 🌮']).fallbackMessage("have a {name}")

        String msg = messageSource.get(msgKey)

        then:
        "have a taco 🌮" == msg
    }
}
