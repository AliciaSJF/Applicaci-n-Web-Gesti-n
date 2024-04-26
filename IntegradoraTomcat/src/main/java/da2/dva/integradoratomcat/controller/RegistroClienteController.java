package da2.dva.integradoratomcat.controller;

import da2.dva.integradoratomcat.model.auxiliar.Direccion;
import da2.dva.integradoratomcat.model.entities.Cliente;
import da2.dva.integradoratomcat.services.ServicioColecciones;
import da2.dva.integradoratomcat.utils.DatosCliente;
import da2.dva.integradoratomcat.utils.DatosContacto;
import da2.dva.integradoratomcat.utils.DatosPersonales;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Set;

@Controller
@RequestMapping("registro/cliente")
public class RegistroClienteController {
    @Autowired
    ServicioColecciones servicio;
    ModelAndView mv = new ModelAndView("/registro/cliente");

    @Bean
    public void conseguirColeccionesRC(){

        mv.addObject("listaPaises",servicio.devuelvePaises());
        System.out.println(servicio.devuelvePaises());
        mv.addObject("listaGeneros",servicio.devuelveGeneros());
        mv.addObject("listaTiposDocumentos",servicio.devuelveTiposDocumentos());
        mv.addObject("listaPreguntas",servicio.devuelvePreguntas());
        mv.addObject("listaTiposVia",servicio.devuelveTiposVia());
        mv.addObject("listaUsuarios",servicio.devuelveUsuarios());
    }

    @GetMapping("paso1")
    public ModelAndView registroCliente(@ModelAttribute("cliente") Cliente cliente, HttpSession sesion, BindingResult result) {
        //mv = new ModelAndView("/registro/cliente");

        mv.addObject("titulo","Registro de cliente");
        mv.addObject("paso" ,"1");
        cliente = (Cliente) sesion.getAttribute("cliente");
        if (cliente != null) mv.addObject("cliente", cliente);
        return mv;
    }

    @PostMapping("paso1")
    public ModelAndView registrar(@Validated(DatosPersonales.class) @ModelAttribute("cliente") Cliente cliente, BindingResult resultado, HttpSession sesion) {
        if (resultado.hasErrors()) {
            mv.addObject("error", "Por favor, rellene los campos obligatorios");
            System.out.println(resultado.getAllErrors().toString());
            return mv;
        }
        Cliente clienteSesion = (Cliente) sesion.getAttribute("cliente");
        if (clienteSesion == null) {
            clienteSesion = new Cliente();
        }
        clienteSesion.setNombre(cliente.getNombre());
        clienteSesion.setApellidos(cliente.getApellidos());
        clienteSesion.setGenero(cliente.getGenero());
        clienteSesion.setFechaNacimiento(cliente.getFechaNacimiento());
        clienteSesion.setPaisNacimiento(cliente.getPaisNacimiento());
        clienteSesion.setTipoDocumento(cliente.getTipoDocumento());
        clienteSesion.setDocumento(cliente.getDocumento());

        sesion.setAttribute("cliente", clienteSesion);
        mv.addObject("paso" ,"2");
        return mv;
    }

    @GetMapping("paso2")
    public ModelAndView paso2(@ModelAttribute("cliente") Cliente cliente, HttpSession sesion, BindingResult result) {
      //  mv = new ModelAndView("/registro/cliente");
        cliente = (Cliente) sesion.getAttribute("cliente");
        if (cliente != null) mv.addObject("cliente", cliente);
        mv.addObject("paso" ,"2");
        return mv;
    }

    @PostMapping("paso2")
    public ModelAndView registrar2(@Validated(DatosContacto.class) @ModelAttribute("cliente") Cliente cliente, BindingResult resultado, HttpSession sesion) {
        if (resultado.hasErrors()) {
            mv.addObject("error", "Por favor, rellene los campos obligatorios");
            return mv;
        }
        Cliente clienteSesion = (Cliente) sesion.getAttribute("cliente");
        if (clienteSesion == null) {
            clienteSesion = new Cliente();
        }

        Direccion direccion = new Direccion();
        //direccion.setTipo_via(cliente.getDireccion().getTipo_via());
        direccion.setNombre_via(cliente.getDireccion().getNombre_via());
        direccion.setNumero_via(cliente.getDireccion().getNumero_via());
        direccion.setPlanta(cliente.getDireccion().getPlanta());
        direccion.setPuerta(cliente.getDireccion().getPuerta());
        direccion.setPortal(cliente.getDireccion().getPortal());
        direccion.setLocalidad(cliente.getDireccion().getLocalidad());
        direccion.setCp(cliente.getDireccion().getCp());
        direccion.setRegion(cliente.getDireccion().getRegion());
        direccion.setPais(cliente.getDireccion().getPais());

        clienteSesion.setDireccion(direccion);
        clienteSesion.getDireccionEntrega().add(direccion);
        clienteSesion.setTelefonoMovil(cliente.getTelefonoMovil());

        //Guardar cliente en la sesion
        sesion.setAttribute("cliente", clienteSesion);

        mv.addObject("paso" ,"3");
        return mv;
    }

    @GetMapping("paso3")
    public ModelAndView paso3(@ModelAttribute("cliente") Cliente cliente, HttpSession sesion, BindingResult result) {
        mv = new ModelAndView("/registro/cliente");
        cliente = (Cliente) sesion.getAttribute("cliente");
        if (cliente != null) mv.addObject("cliente", cliente);
        mv.addObject("paso" ,"3");
        return mv;
    }

    @PostMapping("paso3")
    public ModelAndView registrar3(@Validated(DatosCliente.class) @ModelAttribute("cliente") Cliente cliente, BindingResult resultado, HttpSession sesion) {
        if (resultado.hasErrors()) {
            mv.addObject("error", "Por favor, rellene los campos obligatorios");
            return mv;
        }
        Cliente clienteSesion = (Cliente) sesion.getAttribute("cliente");
        if (clienteSesion == null) {
            clienteSesion = new Cliente();
        }

        clienteSesion.setComentarios(cliente.getComentarios());
        clienteSesion.setAceptacionLicencia(cliente.getAceptacionLicencia());
        System.out.println(cliente.getAceptacionLicencia());

        sesion.setAttribute("cliente", clienteSesion);

        return new ModelAndView("redirect:/registro/cliente/paso4");
    }

    @GetMapping("paso4")
    public ModelAndView resumen(@ModelAttribute("cliente") Cliente cliente, HttpSession sesion, BindingResult result) {

        cliente = (Cliente) sesion.getAttribute("cliente");
        if (cliente == null) {
            cliente = new Cliente();
        }

        //Validar el usaurio para que aparezcan los errores en la vista
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Cliente>> violations = validator.validate(cliente, DatosPersonales.class, DatosContacto.class, DatosCliente.class);
        for(ConstraintViolation<Cliente> violation : violations) {
            String error = violation.getMessage();
            String name = violation.getPropertyPath().toString();
            mv.addObject(name, error);
            System.out.println(name);
            // sesion.setAttribute("error", error);
        }

        mv.addObject("paso" ,"4");

        mv.addObject("cliente", cliente);


        //paso al modelo los errores para guardarlo en un hidden y ver si se puede registrar en el siguiente paso
        if(!violations.isEmpty()){
            mv.addObject("error", "Hay errores");
        }else{
            mv.addObject("error", "No hay errores");
        }
        return mv;
    }

    @PostMapping("paso4")
    public ModelAndView paso4(HttpSession sesion, @ModelAttribute("cliente") Cliente cliente) {
        if (mv.getModel().get("error").equals("No hay errores")) {
            servicio.insertarCliente(cliente);
            mv.setViewName("redirect:/area-cliente");
        }
        return mv;
    }


}
