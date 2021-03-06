/*
 * Copyright 2016 Sumo Logic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Created by yegor on 5/6/16. */

package ws.epigraph.stencils

import ws.epigraph.gen

trait GenStencils {this: gen.GenNames with gen.GenTypes =>

  type GenVarStencil <: GenVarStencilApi

  type GenVarMemberBranch <: GenVarMemberBranchApi

  type GenDataStencil <: GenDataStencilApi

  type GenParams <: GenParamsApi

  type GenParam <: GenParamApi

  type GenParamStencil // either data (for requests), or stencil (for request templates)

  trait GenVarStencilApi {this: GenVarStencil =>

    def varType: GenVarType

    def branches: Seq[GenVarMemberBranch]

  }


  trait GenVarMemberBranchApi {this: GenVarMemberBranch =>

    def member: GenTypeMember

    def stencil: GenDataStencil

  }


  trait GenDataStencilApi {this: GenDataStencil =>

    def dataType: GenDataType

    def params: GenParams

  }


  trait GenParamsApi { this: GenParams =>

    def params: Seq[GenParam]

  }


  trait GenParamApi { this: GenParam =>

    def name: String // TODO ParamName?

    def stencil: GenParamStencil

  }


}

import ws.epigraph.raw

trait RawStencils extends GenStencils {this: raw.RawNames with raw.RawTypes =>

  type GenVarStencil = VarStencil

  type GenVarMemberBranch = VarMemberBranch


  class VarStencil(val varType: GenVarType, val branches: Seq[VarMemberBranch]) extends GenVarStencilApi


  class VarMemberBranch(override val member: GenTypeMember, override val stencil: GenDataStencil) extends GenVarMemberBranchApi


}