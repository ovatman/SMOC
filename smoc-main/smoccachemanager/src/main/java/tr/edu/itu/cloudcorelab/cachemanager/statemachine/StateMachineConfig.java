package tr.edu.itu.cloudcorelab.cachemanager.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import tr.edu.itu.cloudcorelab.cachemanager.utils.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;


@Configuration
@EnableStateMachine(name = Utils.STATE_MACHINE)
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /** Default Constructor **/
    public StateMachineConfig(){ }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states)
            throws Exception {
        states.withStates()
                .initial(States.INITIAL, initializationAction())
                .stateEntry(States.TEACHER_LESSONS,entryActionForWaiting())
                .stateExit(States.TEACHER_LESSONS, exitActionForWaiting())
                .stateEntry(States.TEACHER_STUDENTS,entryActionForWaiting())
                .stateExit(States.TEACHER_STUDENTS, exitActionForWaiting())
                .stateEntry(States.TEACHER_STUDENTS_ST,entryActionForWaiting())
                .stateExit(States.TEACHER_STUDENTS_ST, exitActionForWaiting())
                .stateEntry(States.TEACHER_STUDENTS_ST_LECTURES,entryActionForWaiting())
                .stateExit(States.TEACHER_STUDENTS_ST_LECTURES, exitActionForWaiting())
                .stateEntry(States.TEACHER_LESSONS_LS,entryActionForDone())
                .stateExit(States.TEACHER_LESSONS_LS, exitActionForDone())
                .stateEntry(States.TEACHER_STUDENTS_ST_LECTURES_LS,entryActionForDone())
                .stateExit(States.TEACHER_STUDENTS_ST_LECTURES_LS, exitActionForDone())
                .stateEntry(States.TEACHER_STUDENTS_ST_INFO,entryActionForDone())
                .stateExit(States.TEACHER_STUDENTS_ST_INFO, exitActionForDone());
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
            throws Exception {
        /** Defines "EXTERNAL" type of transitions **/
        transitions
                .withExternal()
                .source(States.INITIAL).target(States.TEACHER_LESSONS)
                .event(Events.TEACHER_TO_LESSONS)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.INITIAL).target(States.TEACHER_STUDENTS)
                .event(Events.TEACHER_TO_STUDENTS)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.TEACHER_LESSONS).target(States.TEACHER_LESSONS_LS)
                .event(Events.LESSONS_TO_LECTURE)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.TEACHER_STUDENTS).target(States.TEACHER_STUDENTS_ST)
                .event(Events.STUDENTS_TO_STUDENT)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.TEACHER_STUDENTS_ST).target(States.TEACHER_STUDENTS_ST_INFO)
                .event(Events.STUDENT_TO_INFO)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.TEACHER_STUDENTS_ST).target(States.TEACHER_STUDENTS_ST_LECTURES)
                .event(Events.STUDENT_TO_LECTURES)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.TEACHER_STUDENTS_ST_LECTURES).target(States.TEACHER_STUDENTS_ST_LECTURES_LS)
                .event(Events.LECTURES_TO_LECTURE)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.TEACHER_STUDENTS_ST_LECTURES_LS).target(States.INITIAL)
                .event(Events.STARTFROMSCRATCH)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.TEACHER_STUDENTS_ST_INFO).target(States.INITIAL)
                .event(Events.STARTFROMSCRATCH)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.TEACHER_LESSONS_LS).target(States.INITIAL)
                .event(Events.STARTFROMSCRATCH)
                .action(transitionAction());
    }

    @Bean
    public Action<States, Events> entryActionForWaiting() {
        return new Action<States, Events>() {

            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("-----------ENTERING WAITING STATE ACTION------------");
                Integer localVar = context.getExtendedState().get("localVarForWaiting", Integer.class);
                localVar = localVar + 2;
                context.getExtendedState().getVariables().put("localVarForWaiting", localVar);
            }
        };
    }

    @Bean
    public Action<States, Events> exitActionForWaiting() {
        return new Action<States, Events>() {

            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("-----------EXITING WAITING STATE ACTION------------");
                Integer localVar = context.getExtendedState().get("localVarForWaiting", Integer.class);
                System.out.println("Local var for waiting state: " + localVar);
            }
        };
    }

    @Bean
    public Action<States, Events> entryActionForDone() {
        return new Action<States, Events>() {

            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("-----------ENTERING DONE STATE ACTION------------");
                Integer localVar = context.getExtendedState().get("localVarForDone", Integer.class);
                localVar = localVar + 5;
                context.getExtendedState().getVariables().put("localVarForDone", localVar);
            }
        };
    }

    @Bean
    public Action<States, Events> exitActionForDone() {
        return new Action<States, Events>() {

            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("-----------EXITING DONE STATE ACTION------------");
                Integer localVar = context.getExtendedState().get("localVarForDone", Integer.class);
                System.out.println("Local var for done state: " + localVar);
            }
        };
    }

    @Bean
    public Action<States, Events> initializationAction() {
        return new Action<States, Events>() {
            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("----------- TRANSITION ACTION FOR INITIALIZATION------------");
                /** Define extended state variable as common variable used inside transition actions **/
                context.getExtendedState().getVariables().put("common", 0);
                /** Define extended state variable as private/local variable used inside state actions **/
                context.getExtendedState().getVariables().put("localVarForWaiting",10);
                context.getExtendedState().getVariables().put("localVarForDone",50);
            }
        };
    }

    @Bean
    public Action<States, Events> transitionAction() {
        return new Action<States, Events>() {
            @Override
            public void execute(StateContext<States, Events> context) {
                System.out.println("-----------TRANSITION ACTION FOR INCREASING VARIABLE------------");

               /* Object sleep = context.getMessageHeaders().get("timeSleep");
                long longSleep = ((Number) sleep).longValue();

                Map<Object, Object> variables = context.getExtendedState().getVariables();
                Integer commonVar = context.getExtendedState().get("common", Integer.class);

                if (commonVar == 0) {
                    logger.info("Switch common variable from 0 to 1");
                    variables.put("common", 1);
                    sleepForAWhile(longSleep);
                } else if (commonVar == 1) {
                    logger.info("Switch common variable from 1 to 2");
                    variables.put("common", 2);
                    sleepForAWhile(longSleep);
                } else if (commonVar == 2) {
                    logger.info("Switch common variable from 2 to 0");
                    variables.put("common", 0);
                    sleepForAWhile(longSleep);
                }
                */
            }
        };
    }

    public void sleepForAWhile(Long sleepTime){
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException ex) {
            // handle error
        }

    }



}
