package mars.mips.instructions.syscalls;

import mars.Globals;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;
import mars.util.Binary;
import mars.util.SystemIO;

public class SyscallPrintF extends AbstractSyscall {
	   /**
	    * Build an instance of the Print String syscall.  Default service number
	    * is 4 and name is "PrintString".
	    */
	       public SyscallPrintF() {
	         super(18, "PrintF");
	      }
	      
	   /**
	   * Performs syscall function to print string stored starting at address in $a0.
	   */
	       public void simulate(ProgramStatement statement) throws ProcessingException {
	         int byteAddress = RegisterFile.getValue(4);
	         char ch = 0;
	         int count=1;
	         try
	         {
	            ch = (char) Globals.memory.getByte(byteAddress);
	                              // won't stop until NULL byte reached!
	            while (ch != 0)
	            {
	            	if(ch == '%'){
	            		count=printValue((char) Globals.memory.getByte(++byteAddress),count);
	            	}
	               SystemIO.printString(new Character(ch).toString());
	               byteAddress++;
	               ch = (char) Globals.memory.getByte(byteAddress);
	            }
	         } 
	             catch (AddressErrorException e)
	            {
	               throw new ProcessingException(statement, e);
	            }
	      }

	private int printValue(char byte1, int count) {
		int value=0;
		int stack=RegisterFile.getValue(29);
		if(count<3){
			value=RegisterFile.getValue(4+count);
		}else{
			try {
				value=Globals.memory.getWord(stack+((count-4)*4));
			} catch (AddressErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(byte1=='d') SystemIO.printString(new Integer(value).toString());
		else if(byte1=='f'){
			SystemIO.printString(new Float(Float.intBitsToFloat(
                    value)).toString());
		}else if(byte1=='l'){
				SystemIO.printString(new Double(Double.longBitsToDouble(
		             Binary.twoIntsToLong(RegisterFile.getValue(4+count+1),value)
		             )).toString());
					count++;
		}
		return count;
	}
	   }
