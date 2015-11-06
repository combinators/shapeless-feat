/* 
 * Copyright (c) 2015 Jan Bessai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shapeless.feat

import org.scalatest._
import org.scalatest.prop._
import org.scalatest.matchers._
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Properties
import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class KnownInstanceTest extends FreeSpec with GeneratorDrivenPropertyChecks with Matchers {
  import enumerables._
  import ArbitraryInstances._
  import EnumerableInstances._
  
  implicit override val generatorDrivenConfig =
    PropertyCheckConfig(maxSize = 4)
  
  "Checking known instances" - {
    "NonRec type" in {
      forAll { (x: NonRec) =>
        nonRec.enumerate.values.flatMap(_._2) should contain (x)
      }
    }
    "Rec type" in {
      forAll { (x: Rec) =>
        rec.enumerate.values.flatMap(_._2) should contain (x)
      }
    }
    
    
    /* TODO: These take to long because of linear search through
     * the entire space of inhabitants. Ideas to fix this are:
     * - Sorting inhabitants (how?) and performing binary search 
     * - Computing indices of random samples and looking them up directly
     */
     
    "MutualRec1 type" ignore {
      forAll { (x: MutualRec1) =>
        mutualRec1.enumerate.values.flatMap(_._2) should contain (x)
      }
    }
    "MutualRec2 type" ignore {
      forAll { (x: MutualRec2) =>
        mutualRec1.enumerate.values.flatMap(_._2) should contain (x)
      }
    }
    "Tree type" ignore {
       forAll { (x: Tree) =>
        tree.enumerate.values.flatMap(_._2) should contain (x)
      }
    }
  }
}