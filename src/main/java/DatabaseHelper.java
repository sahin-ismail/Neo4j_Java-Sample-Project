import org.neo4j.driver.*;
import org.neo4j.driver.util.Pair;

import java.lang.Record;
import java.sql.ResultSet;
import java.util.List;
import java.util.Scanner;

import static org.neo4j.driver.Values.parameters;
public class DatabaseHelper {
    Scanner input = new Scanner(System.in);

    private Driver driver;
    private Session session;

    private String uri;
    private String username;
    private String password;

    public DatabaseHelper(){
        username = askUsername();
        password = askPassword();
        uri = "bolt://localhost:7687";

        try {
            driver = GraphDatabase.driver(uri,AuthTokens.basic(username, password));
        }catch (Exception e){

        }

        createBank();
    }

    public void addConstraints() {
        try {
            session = driver.session();
            session.run("CREATE CONSTRAINT ON (a:Customer) ASSERT a.id IS UNIQUE ");
            session.run("CREATE CONSTRAINT ON (a.Account) ASSERT a.acc_num IS UNIQUE ");
        }catch (Exception e){

        }
        finally {
            session.close();
        }
    }

    private String askUsername(){
        System.out.print("Enter username: ");
        return (input.nextLine());
    }

    private String askPassword(){
        System.out.print("Enter password: ");
        return (input.nextLine());
    }

    private void createBank(){
        final String name = "Bank";

        try {
            session = driver.session();
            session.run("MERGE(:Bank {name: $name})",
                    parameters("name",name));
        }catch (Exception e){

        }
        finally {
            session.close();
        }
    }

    public void createEmployee(Employee employee){
        final String username = employee.getUsername();
        final String password = employee.getPassword();

        try {
            session = driver.session();

            session.run("MERGE (:Employee {username: $username, password: $password})",
                    parameters("username",username,"password",password));

            session.run("MATCH (a:Bank), (b:Employee)" +
                    "WHERE a.name = 'Bank' AND b.username = $username " +
                    "MERGE (b)-[r:Employee_Of]->(a)",
                    parameters("username",username));

        }catch (Exception e){

        }
        finally {
            session.close();
        }
    }

    public void validateEmployee(final String username, final String password){
        Employee employee = new Employee(username,password);

        try {
            session = driver.session();

            Result result = session.run("MATCH (n: Employee) " +
                    "WHERE n.username = $username AND n.password = $password " +
                    "RETURN n",
                    parameters("username",username,"password",password));


//            Record res = result.next();
//            List<org.neo4j.driver.util.Pair<String,Value>> values = res.fields();
//            for (Pair<String, Value> nameValue: values) {
//                if("n".equals(nameValue.key())){
//                    Value value = nameValue.value();
//
//                    employee.setUsername(value.get("username").asString());
//                    employee.setPassword(value.get("password").asString());
//                }
//            }
        }catch (Exception e){

        }
        finally {
            session.close();
        }
    }

    public boolean deleteCustomerAccount(int customerId, int accNum){
        int flag = 0;
        try {
            session = driver.session();

            session.run("MATCH (:Customer {id: $id})->[r:Customer_Of]-(:Bank) DELETE r",
                    parameters("id",customerId));

            session.run("MATCH (:Account {acc_num: $accNum})-[r:Account_of]-(:Customer {id: $id}) DELETE r",
                    parameters("id",customerId,"accNum",accNum));

            session.run("MATCH (a:Customer {id: $id}), (b:Account {acc_num: $accNum}) DELETE a, b",
                    parameters("id", customerId, "accNum", accNum));

            flag = 1;
        }catch (Exception e){

        }
        finally {
            session.close();
        }
        if (flag == 1)
            return true;
        return false;
    }

    public boolean updateCustomerAccount(String ph, int id) {
        int flag = 0;
        try {
            session = driver.session();

            session.run("MATCH (a:Customer {id: $id}) SET a.ph_num = $ph",
                    parameters("id",id,"ph",ph));
            flag = 1;
        }catch (Exception e){

        }
        finally {
            session.close();
        }
        if (flag == 1)
            return true;
        return false;
    }

}
