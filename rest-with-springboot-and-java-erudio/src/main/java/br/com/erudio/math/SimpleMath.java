package br.com.erudio.math;

import br.com.erudio.exception.UnsupportedMathOperationException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

public class SimpleMath {

    //http://localhost:8080/math/sum/3/5
    public Double sum(Double numberOne, Double numberTwo) {

        return numberOne + numberTwo;
    }

    //http://localhost:8080/math/subtraction/3/5
    public Double subtraction(Double numberOne, Double numberTwo) {

        return numberOne - numberTwo;
    }

    //http://localhost:8080/math/division/3/5
    public Double division(Double numberOne, Double numberTwo) {

        return numberOne / numberTwo;
    }

    //http://localhost:8080/math/multiplication/3/5
    public Double multiplication(Double numberOne, Double numberTwo) {

        return numberOne * numberTwo;
    }

    //http://localhost:8080/math/mean/3/5
    public Double mean(Double numberOne, Double numberTwo) {

        return (numberOne + numberTwo) / 2;
    }

    //http://localhost:8080/math/squareRoot/81
    public Double squareRoot(Double number) {

        return Math.sqrt(number);
    }
}
